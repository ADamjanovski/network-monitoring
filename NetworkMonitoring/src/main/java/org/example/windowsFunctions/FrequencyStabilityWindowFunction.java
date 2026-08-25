package org.example.windowsFunctions;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.example.models.FrequencyAlert;
import org.example.models.Measurement;
import org.example.models.configuration.AppConfig;
import org.example.models.enums.FrequencyAlertType;
import org.example.models.enums.FrequencyIncidentState;
import org.example.models.enums.SeverityLevel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FrequencyStabilityWindowFunction extends
        ProcessWindowFunction<Measurement, FrequencyAlert, String, TimeWindow> {

    private static final ValueStateDescriptor<IncidentTracker> INCIDENT_TRACKER =
            new ValueStateDescriptor<>("system-frequency-incident", IncidentTracker.class);

    @Override
    public void process(
            String systemKey,
            Context context,
            Iterable<Measurement> measurements,
            Collector<FrequencyAlert> out) throws Exception {

        List<Measurement> windowData = new ArrayList<>();
        measurements.forEach(windowData::add);

        FrequencyAssessment assessment = assess(
                windowData,
                context.window().getStart(),
                context.window().getEnd()
        );
        if (assessment == null) return;

        ValueState<IncidentTracker> state = context.globalState().getState(INCIDENT_TRACKER);
        IncidentTracker tracker = state.value();
        if (tracker == null) tracker = new IncidentTracker();

        FrequencyAlert transition = advanceIncident(tracker, assessment);
        state.update(tracker);
        if (transition != null) out.collect(transition);
    }

    static FrequencyAssessment assess(List<Measurement> measurements, long windowStart, long windowEnd) {
        List<ReportingFrame> frames = aggregateReportingFrames(measurements);
        if (frames.size() < 2) return null;

        FrequencyStatistics stats = calculateFrequencyStatistics(frames);
        double rocof = calculateRoCoF(frames);
        double rocofVolatility = calculateRoCoFVolatility(frames, rocof);
        List<String> affectedRegions = findAffectedRegions(measurements);

        boolean hasFrequencyDeviation =
                Math.abs(stats.average - AppConfig.NOMINAL_FREQUENCY) > AppConfig.FREQ_WARNING_THRESHOLD;
        boolean hasCriticalRoCoF = Math.abs(rocof) > AppConfig.ROCOF_CRITICAL_THRESHOLD;
        boolean hasWarningRoCoF = Math.abs(rocof) > AppConfig.ROCOF_WARNING_THRESHOLD;
        boolean hasRegionalDisagreement = !affectedRegions.isEmpty();

        FrequencyAlertType alertType = null;
        SeverityLevel severityLevel = SeverityLevel.LOW;
        if (hasCriticalRoCoF) {
            alertType = FrequencyAlertType.CRITICAL_ROCOF;
            severityLevel = SeverityLevel.CRITICAL;
        } else if (hasWarningRoCoF) {
            alertType = FrequencyAlertType.HIGH_ROCOF;
            severityLevel = SeverityLevel.HIGH;
        } else if (hasFrequencyDeviation || hasRegionalDisagreement) {
            alertType = FrequencyAlertType.FREQUENCY_DEVIATION;
            severityLevel = SeverityLevel.MEDIUM;
        }

        double severityScore = alertType == null
                ? 0.0
                : calculateSeverity(stats.average, rocof, hasCriticalRoCoF);
        if (hasRegionalDisagreement) severityScore = Math.max(0.3, severityScore);

        return new FrequencyAssessment(
                windowStart,
                windowEnd,
                stats.average,
                stats.min,
                stats.max,
                rocof,
                rocofVolatility,
                measurements.size(),
                alertType,
                severityLevel,
                severityScore,
                affectedRegions
        );
    }

    static FrequencyAlert advanceIncident(IncidentTracker tracker, FrequencyAssessment assessment) {
        long now = assessment.windowEnd;

        if (tracker.phase == FrequencyIncidentState.CLOSE && now >= tracker.cooldownUntil) {
            tracker.reset();
        }

        if (tracker.incidentId == null) {
            if (!assessment.isAbnormal()) return null;

            tracker.incidentId = "frequency-incident-" + now;
            tracker.incidentStartedAt = now;
            tracker.phase = FrequencyIncidentState.START;
            tracker.captureClassification(assessment);
            tracker.lastEmittedAt = now;
            return createAlert(tracker, assessment, FrequencyIncidentState.START, true);
        }

        if (tracker.phase == FrequencyIncidentState.CLOSE) return null;

        if (assessment.isAbnormal()) {
            boolean resumedDuringRecovery = tracker.phase == FrequencyIncidentState.RECOVERY;
            boolean escalated = assessment.severityLevel.ordinal() > tracker.lastEmittedSeverity.ordinal();
            boolean updateDue = now - tracker.lastEmittedAt >= AppConfig.FREQUENCY_INCIDENT_UPDATE_INTERVAL_MS;

            if (!resumedDuringRecovery && !escalated && !updateDue) return null;

            tracker.phase = FrequencyIncidentState.UPDATE;
            tracker.recoveryStartedAt = 0L;
            tracker.captureClassification(assessment);
            tracker.lastEmittedAt = now;
            return createAlert(tracker, assessment, FrequencyIncidentState.UPDATE, true);
        }

        if (tracker.phase == FrequencyIncidentState.START || tracker.phase == FrequencyIncidentState.UPDATE) {
            tracker.phase = FrequencyIncidentState.RECOVERY;
            tracker.recoveryStartedAt = now;
            tracker.lastEmittedAt = now;
            return createAlert(tracker, assessment, FrequencyIncidentState.RECOVERY, false);
        }

        if (tracker.phase == FrequencyIncidentState.RECOVERY &&
                now - tracker.recoveryStartedAt >= AppConfig.FREQUENCY_INCIDENT_RECOVERY_HOLD_MS) {
            tracker.phase = FrequencyIncidentState.CLOSE;
            tracker.cooldownUntil = now + AppConfig.FREQUENCY_INCIDENT_COOLDOWN_MS;
            tracker.lastEmittedAt = now;
            return createAlert(tracker, assessment, FrequencyIncidentState.CLOSE, false);
        }

        return null;
    }

    private static FrequencyAlert createAlert(
            IncidentTracker tracker,
            FrequencyAssessment assessment,
            FrequencyIncidentState incidentState,
            boolean activeCondition) {
        FrequencyAlert alert = new FrequencyAlert();
        alert.setAlertId(tracker.incidentId);
        alert.setWindowStart(tracker.incidentStartedAt);
        alert.setWindowEnd(assessment.windowEnd);
        alert.setTimestamp(assessment.windowEnd);
        alert.setRegion("System");
        alert.setIncidentState(incidentState);
        alert.setAffectedRegions(assessment.affectedRegions);
        alert.setAvgFrequency(assessment.average);
        alert.setMinFrequency(assessment.min);
        alert.setMaxFrequency(assessment.max);
        alert.setFrequencyDeviation(assessment.average - AppConfig.NOMINAL_FREQUENCY);
        alert.setRocof(assessment.rocof);
        alert.setRocofVolatility(assessment.rocofVolatility);
        alert.setMeasurementCount(assessment.measurementCount);
        alert.setAlertType(activeCondition ? assessment.alertType : tracker.lastAlertType);
        alert.setSeverityLevel(activeCondition ? assessment.severityLevel : SeverityLevel.LOW);
        alert.setSeverityScore(activeCondition ? assessment.severityScore : 0.0);
        alert.setMessage(buildMessage(tracker.incidentId, incidentState, assessment));
        return alert;
    }

    private static String buildMessage(
            String incidentId,
            FrequencyIncidentState state,
            FrequencyAssessment assessment) {
        String message = String.format(
                "%s: System frequency incident %s. Average=%.4f Hz, RoCoF=%+.4f Hz/s.",
                state.name(), incidentId, assessment.average, assessment.rocof
        );
        if (!assessment.affectedRegions.isEmpty()) {
            message += " Statistically significant regional disagreement: " +
                    String.join(", ", assessment.affectedRegions) + ".";
        }
        return message;
    }

    static List<String> findAffectedRegions(List<Measurement> measurements) {
        Map<String, List<Measurement>> measurementsByRegion = new TreeMap<>();
        for (Measurement measurement : measurements) {
            if (measurement.getRegion() == null) continue;
            measurementsByRegion
                    .computeIfAbsent(measurement.getRegion(), ignored -> new ArrayList<>())
                    .add(measurement);
        }

        Map<String, Double> regionalEstimates = new TreeMap<>();
        List<Double> spatialNoiseSamples = new ArrayList<>();
        for (Map.Entry<String, List<Measurement>> entry : measurementsByRegion.entrySet()) {
            List<ReportingFrame> frames = aggregateReportingFrames(entry.getValue());
            if (frames.isEmpty()) continue;

            double sum = 0.0;
            for (ReportingFrame frame : frames) sum += frame.frequency;
            regionalEstimates.put(entry.getKey(), sum / frames.size());
            spatialNoiseSamples.addAll(calculateFrameMads(entry.getValue()));
        }

        if (regionalEstimates.size() < 3) return Collections.emptyList();

        List<Double> estimates = new ArrayList<>(regionalEstimates.values());
        double center = median(estimates);
        double robustNoiseSigma = spatialNoiseSamples.isEmpty()
                ? 0.0
                : 1.4826 * median(spatialNoiseSamples);
        double disagreementThreshold = Math.max(
                AppConfig.REGIONAL_FREQUENCY_DISAGREEMENT_MIN_HZ,
                AppConfig.REGIONAL_FREQUENCY_MAD_MULTIPLIER * robustNoiseSigma
        );

        List<String> affectedRegions = new ArrayList<>();
        for (Map.Entry<String, Double> entry : regionalEstimates.entrySet()) {
            if (Math.abs(entry.getValue() - center) > disagreementThreshold) {
                affectedRegions.add(entry.getKey());
            }
        }
        return affectedRegions;
    }

    private static List<Double> calculateFrameMads(List<Measurement> measurements) {
        Map<Long, List<Double>> frequenciesByTimestamp = new TreeMap<>();
        for (Measurement measurement : measurements) {
            double frequency = measurement.getFrequency();
            if (Double.isNaN(frequency) || Double.isInfinite(frequency)) continue;
            frequenciesByTimestamp
                    .computeIfAbsent(measurement.getTimestamp(), ignored -> new ArrayList<>())
                    .add(frequency);
        }

        List<Double> frameMads = new ArrayList<>();
        for (List<Double> frequencies : frequenciesByTimestamp.values()) {
            if (frequencies.size() < 2) continue;
            double frameMedian = median(frequencies);
            List<Double> deviations = new ArrayList<>();
            for (double frequency : frequencies) {
                deviations.add(Math.abs(frequency - frameMedian));
            }
            frameMads.add(median(deviations));
        }
        return frameMads;
    }

    private static double median(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int middle = sorted.size() / 2;
        return sorted.size() % 2 == 0
                ? (sorted.get(middle - 1) + sorted.get(middle)) / 2.0
                : sorted.get(middle);
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
            frames.add(new ReportingFrame(entry.getKey(), median(entry.getValue())));
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
            sumT += t;
            sumF += f;
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
        for (double rocof : instantRocofs) {
            double diff = rocof - avgRocof;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / instantRocofs.size());
    }

    private static FrequencyStatistics calculateFrequencyStatistics(List<ReportingFrame> frames) {
        double sum = 0, min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (ReportingFrame frame : frames) {
            sum += frame.frequency;
            min = Math.min(min, frame.frequency);
            max = Math.max(max, frame.frequency);
        }
        return new FrequencyStatistics(sum / frames.size(), min, max);
    }

    private static double calculateSeverity(double average, double rocof, boolean critical) {
        double frequencyScore = Math.min(1.0,
                Math.abs(average - AppConfig.NOMINAL_FREQUENCY));
        double rocofScore = Math.min(1.0,
                Math.abs(rocof) / AppConfig.ROCOF_CRITICAL_THRESHOLD);
        double severity = 0.4 * frequencyScore + 0.6 * rocofScore;
        return critical ? Math.min(1.0, severity * 1.3) : severity;
    }

    static class ReportingFrame {
        final long timestamp;
        final double frequency;

        ReportingFrame(long timestamp, double frequency) {
            this.timestamp = timestamp;
            this.frequency = frequency;
        }
    }

    static class FrequencyAssessment {
        final long windowStart;
        final long windowEnd;
        final double average;
        final double min;
        final double max;
        final double rocof;
        final double rocofVolatility;
        final int measurementCount;
        final FrequencyAlertType alertType;
        final SeverityLevel severityLevel;
        final double severityScore;
        final List<String> affectedRegions;

        FrequencyAssessment(
                long windowStart, long windowEnd, double average, double min, double max,
                double rocof, double rocofVolatility, int measurementCount,
                FrequencyAlertType alertType, SeverityLevel severityLevel, double severityScore,
                List<String> affectedRegions) {
            this.windowStart = windowStart;
            this.windowEnd = windowEnd;
            this.average = average;
            this.min = min;
            this.max = max;
            this.rocof = rocof;
            this.rocofVolatility = rocofVolatility;
            this.measurementCount = measurementCount;
            this.alertType = alertType;
            this.severityLevel = severityLevel;
            this.severityScore = severityScore;
            this.affectedRegions = new ArrayList<>(affectedRegions);
        }

        boolean isAbnormal() {
            return alertType != null;
        }
    }

    static class IncidentTracker implements Serializable {
        private static final long serialVersionUID = 1L;

        String incidentId;
        long incidentStartedAt;
        FrequencyIncidentState phase;
        long lastEmittedAt;
        long recoveryStartedAt;
        long cooldownUntil;
        FrequencyAlertType lastAlertType;
        SeverityLevel lastEmittedSeverity;

        IncidentTracker() {
        }

        void captureClassification(FrequencyAssessment assessment) {
            lastAlertType = assessment.alertType;
            lastEmittedSeverity = assessment.severityLevel;
        }

        void reset() {
            incidentId = null;
            incidentStartedAt = 0L;
            phase = null;
            lastEmittedAt = 0L;
            recoveryStartedAt = 0L;
            cooldownUntil = 0L;
            lastAlertType = null;
            lastEmittedSeverity = null;
        }
    }

    private static class FrequencyStatistics {
        final double average;
        final double min;
        final double max;

        FrequencyStatistics(double average, double min, double max) {
            this.average = average;
            this.min = min;
            this.max = max;
        }
    }
}
