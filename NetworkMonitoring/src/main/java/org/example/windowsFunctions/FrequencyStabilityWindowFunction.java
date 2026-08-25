package org.example.windowsFunctions;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.example.models.FrequencyAlert;
import org.example.models.Measurement;
import org.example.models.configuration.AppConfig;
import org.example.models.enums.FrequencyAlertType;
import org.example.models.enums.SeverityLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class FrequencyStabilityWindowFunction extends
        ProcessWindowFunction<Measurement, FrequencyAlert, String, TimeWindow> {

    @Override
    public void process(
            String region,
            Context context,
            Iterable<Measurement> measurements,
            Collector<FrequencyAlert> out) throws Exception {

        List<Measurement> windowData = new ArrayList<>();
        measurements.forEach(windowData::add);

        List<ReportingFrame> frames = aggregateReportingFrames(windowData);
        if (frames.size() < 2) return;

        FrequencyStatistics stats = calculateFrequencyStatistics(frames);
        double rocof           = calculateRoCoF(frames);
        double rocofVolatility = calculateRoCoFVolatility(frames, rocof);

        boolean hasFrequencyDeviation = Math.abs(stats.average - AppConfig.NOMINAL_FREQUENCY) > AppConfig.FREQ_WARNING_THRESHOLD;
        boolean hasCriticalRoCoF      = Math.abs(rocof) > AppConfig.ROCOF_CRITICAL_THRESHOLD;
        boolean hasWarningRoCoF       = Math.abs(rocof) > AppConfig.ROCOF_WARNING_THRESHOLD;

        if (!hasFrequencyDeviation && !hasWarningRoCoF && !hasCriticalRoCoF) return;

        FrequencyAlert alert = new FrequencyAlert();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setWindowStart(context.window().getStart());
        alert.setWindowEnd(context.window().getEnd());
        alert.setTimestamp(context.window().getEnd());
        alert.setRegion(region);
        alert.setAvgFrequency(stats.average);
        alert.setMinFrequency(stats.min);
        alert.setMaxFrequency(stats.max);
        alert.setFrequencyDeviation(stats.average - AppConfig.NOMINAL_FREQUENCY);
        alert.setRocof(rocof);
        alert.setRocofVolatility(rocofVolatility);
        alert.setMeasurementCount(windowData.size());

        if (hasCriticalRoCoF) {
            alert.setAlertType(FrequencyAlertType.CRITICAL_ROCOF);
            alert.setSeverityLevel(SeverityLevel.CRITICAL);
            alert.setMessage(String.format(
                    "CRITICAL: Region %s - RoCoF=%.4f Hz/s exceeds ±%.1f Hz/s. " +
                            "Avg freq=%.4f Hz. Samples=%d. Cascade failure risk!",
                    region, rocof, AppConfig.ROCOF_CRITICAL_THRESHOLD, stats.average, windowData.size()));

        } else if (hasWarningRoCoF) {
            alert.setAlertType(FrequencyAlertType.HIGH_ROCOF);
            alert.setSeverityLevel(SeverityLevel.HIGH);
            alert.setMessage(String.format(
                    "WARNING: Region %s - RoCoF=%.4f Hz/s exceeds ±%.1f Hz/s. " +
                            "Avg freq=%.4f Hz. Samples=%d. Possible generator trip.",
                    region, rocof, AppConfig.ROCOF_WARNING_THRESHOLD, stats.average, windowData.size()));

        } else {
            alert.setAlertType(FrequencyAlertType.FREQUENCY_DEVIATION);
            alert.setSeverityLevel(SeverityLevel.MEDIUM);
            alert.setMessage(String.format(
                    "WARNING: Region %s - Frequency deviation=%.4f Hz. " +
                            "Avg=%.4f Hz (range: %.4f - %.4f Hz). Samples=%d.",
                    region, stats.average - AppConfig.NOMINAL_FREQUENCY,
                    stats.average, stats.min, stats.max, windowData.size()));
        }

        alert.setSeverityScore(calculateSeverity(stats.average, rocof, hasCriticalRoCoF));

        out.collect(alert);
    }

    static List<ReportingFrame> aggregateReportingFrames(List<Measurement> measurements) {
        Map<Long, List<Double>> frequenciesByTimestamp = new TreeMap<>();

        for (Measurement measurement : measurements) {
            double frequency = measurement.getFrequency();
            if (Double.isNaN(frequency) || Double.isInfinite(frequency)) continue;

            frequenciesByTimestamp
                    .computeIfAbsent(measurement.getTimestamp(), ignored -> new ArrayList<>())
                    .add(frequency);
        }

        List<ReportingFrame> frames = new ArrayList<>();
        for (Map.Entry<Long, List<Double>> entry : frequenciesByTimestamp.entrySet()) {
            List<Double> frameFrequencies = entry.getValue();
            Collections.sort(frameFrequencies);
            int middle = frameFrequencies.size() / 2;
            double median = frameFrequencies.size() % 2 == 0
                    ? (frameFrequencies.get(middle - 1) + frameFrequencies.get(middle)) / 2.0
                    : frameFrequencies.get(middle);
            frames.add(new ReportingFrame(entry.getKey(), median));
        }

        return frames;
    }

    static double calculateRoCoF(List<ReportingFrame> frames) {
        int n = frames.size();
        long baseTime = frames.get(0).timestamp;

        double sumT = 0, sumF = 0, sumTF = 0, sumT2 = 0;

        for (ReportingFrame frame : frames) {
            double t = (frame.timestamp - baseTime) / 1000.0;
            double f = frame.frequency;
            sumT  += t;
            sumF  += f;
            sumTF += t * f;
            sumT2 += t * t;
        }

        double denominator = n * sumT2 - sumT * sumT;
        if (Math.abs(denominator) < 1e-10) return 0.0;

        return (n * sumTF - sumT * sumF) / denominator;
    }

    static double calculateRoCoFVolatility(List<ReportingFrame> frames, double avgRocof) {
        if (frames.size() < 2) return 0.0;

        List<Double> instantRocofs = new ArrayList<>();
        for (int i = 1; i < frames.size(); i++) {
            double dt = (frames.get(i).timestamp - frames.get(i - 1).timestamp) / 1000.0;
            double df = frames.get(i).frequency - frames.get(i - 1).frequency;
            if (dt > 0) instantRocofs.add(df / dt);
        }

        if (instantRocofs.isEmpty()) return 0.0;

        double sumSquaredDiff = 0;
        for (double r : instantRocofs) {
            double diff = r - avgRocof;
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff / instantRocofs.size());
    }

    private FrequencyStatistics calculateFrequencyStatistics(List<ReportingFrame> frames) {
        double sum = 0, min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (ReportingFrame frame : frames) {
            double f = frame.frequency;
            sum += f;
            if (f < min) min = f;
            if (f > max) max = f;
        }
        return new FrequencyStatistics(sum / frames.size(), min, max);
    }

    private double calculateSeverity(double avgFrequency, double rocof, boolean isCritical) {
        double freqScore  = Math.min(1.0, Math.abs(avgFrequency - AppConfig.NOMINAL_FREQUENCY) / 1.0);
        double rocofScore = Math.min(1.0, Math.abs(rocof) / AppConfig.ROCOF_CRITICAL_THRESHOLD);
        double severity   = 0.4 * freqScore + 0.6 * rocofScore;
        return isCritical ? Math.min(1.0, severity * 1.3) : severity;
    }

    static class ReportingFrame {
        final long timestamp;
        final double frequency;

        ReportingFrame(long timestamp, double frequency) {
            this.timestamp = timestamp;
            this.frequency = frequency;
        }
    }

    private static class FrequencyStatistics {
        double average, min, max;
        FrequencyStatistics(double average, double min, double max) {
            this.average = average;
            this.min     = min;
            this.max     = max;
        }
    }
}
