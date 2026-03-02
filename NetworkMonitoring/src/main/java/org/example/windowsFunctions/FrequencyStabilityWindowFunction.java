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
import java.util.List;
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
        if (windowData.size() < 10) return;

        windowData.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

        FrequencyStatistics stats = calculateFrequencyStatistics(windowData);
        double rocof           = calculateRoCoF(windowData);
        double rocofVolatility = calculateRoCoFVolatility(windowData, rocof);

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

    private double calculateRoCoF(List<Measurement> data) {
        int n = data.size();
        long baseTime = data.get(0).getTimestamp();

        double sumT = 0, sumF = 0, sumTF = 0, sumT2 = 0;

        for (Measurement m : data) {
            double t = (m.getTimestamp() - baseTime) / 1000.0;
            double f = m.getFrequency();
            sumT  += t;
            sumF  += f;
            sumTF += t * f;
            sumT2 += t * t;
        }

        double denominator = n * sumT2 - sumT * sumT;
        if (Math.abs(denominator) < 1e-10) return 0.0;

        return (n * sumTF - sumT * sumF) / denominator;
    }

    private double calculateRoCoFVolatility(List<Measurement> data, double avgRocof) {
        if (data.size() < 2) return 0.0;

        List<Double> instantRocofs = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            double dt = (data.get(i).getTimestamp() - data.get(i - 1).getTimestamp()) / 1000.0;
            double df =  data.get(i).getFrequency()  - data.get(i - 1).getFrequency();
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

    private FrequencyStatistics calculateFrequencyStatistics(List<Measurement> data) {
        double sum = 0, min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (Measurement m : data) {
            double f = m.getFrequency();
            sum += f;
            if (f < min) min = f;
            if (f > max) max = f;
        }
        return new FrequencyStatistics(sum / data.size(), min, max);
    }

    private double calculateSeverity(double avgFrequency, double rocof, boolean isCritical) {
        double freqScore  = Math.min(1.0, Math.abs(avgFrequency - AppConfig.NOMINAL_FREQUENCY) / 1.0);
        double rocofScore = Math.min(1.0, Math.abs(rocof) / AppConfig.ROCOF_CRITICAL_THRESHOLD);
        double severity   = 0.4 * freqScore + 0.6 * rocofScore;
        return isCritical ? Math.min(1.0, severity * 1.3) : severity;
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