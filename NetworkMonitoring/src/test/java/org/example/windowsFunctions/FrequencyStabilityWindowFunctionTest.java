package org.example.windowsFunctions;

import org.example.models.Measurement;
import org.example.models.FrequencyAlert;
import org.example.models.enums.FrequencyAlertType;
import org.example.models.enums.FrequencyIncidentState;
import org.example.models.enums.SeverityLevel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FrequencyStabilityWindowFunctionTest {

    @Test
    public void aggregatesEachTimestampUsingSpatialMedian() {
        List<Measurement> measurements = Arrays.asList(
                measurement(1000L, 49.98),
                measurement(1000L, 50.00),
                measurement(1000L, 50.01),
                measurement(1000L, 50.02),
                measurement(1000L, 55.00),
                measurement(2000L, 49.90),
                measurement(2000L, 49.91),
                measurement(2000L, 49.92)
        );

        List<FrequencyStabilityWindowFunction.ReportingFrame> frames =
                FrequencyStabilityWindowFunction.aggregateReportingFrames(measurements);

        assertEquals(2, frames.size());
        assertEquals(1000L, frames.get(0).timestamp);
        assertEquals(50.01, frames.get(0).frequency, 0.000001);
        assertEquals(2000L, frames.get(1).timestamp);
        assertEquals(49.91, frames.get(1).frequency, 0.000001);
    }

    @Test
    public void calculatesRocofBetweenReportingFrames() {
        List<FrequencyStabilityWindowFunction.ReportingFrame> frames = Arrays.asList(
                frame(1000L, 50.0),
                frame(2000L, 49.5),
                frame(3000L, 49.0)
        );

        double rocof = FrequencyStabilityWindowFunction.calculateRoCoF(frames);
        double volatility = FrequencyStabilityWindowFunction.calculateRoCoFVolatility(frames, rocof);

        assertEquals(-0.5, rocof, 0.000001);
        assertEquals(0.0, volatility, 0.000001);
    }

    @Test
    public void simultaneousPmuNoiseDoesNotBecomeRocof() {
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(measurement(1000L, 49.98));
        measurements.add(measurement(1000L, 50.00));
        measurements.add(measurement(1000L, 50.50));
        measurements.add(measurement(2000L, 49.50));
        measurements.add(measurement(2000L, 50.00));
        measurements.add(measurement(2000L, 50.02));

        List<FrequencyStabilityWindowFunction.ReportingFrame> frames =
                FrequencyStabilityWindowFunction.aggregateReportingFrames(measurements);
        double rocof = FrequencyStabilityWindowFunction.calculateRoCoF(frames);
        double volatility = FrequencyStabilityWindowFunction.calculateRoCoFVolatility(frames, rocof);

        assertEquals(0.0, rocof, 0.000001);
        assertEquals(0.0, volatility, 0.000001);
    }

    @Test
    public void emitsOneDeduplicatedIncidentLifecycleWithCooldown() {
        FrequencyStabilityWindowFunction.IncidentTracker tracker =
                new FrequencyStabilityWindowFunction.IncidentTracker();

        FrequencyAlert start = FrequencyStabilityWindowFunction.advanceIncident(
                tracker, abnormalAssessment(1000L, SeverityLevel.MEDIUM));
        String incidentId = start.getAlertId();
        assertEquals(FrequencyIncidentState.START, start.getIncidentState());
        assertTrue(start.toJson().contains("\"incident_state\":\"START\""));
        assertTrue(start.toJson().contains("\"affected_regions\":[]"));

        assertNull(FrequencyStabilityWindowFunction.advanceIncident(
                tracker, abnormalAssessment(2000L, SeverityLevel.MEDIUM)));

        FrequencyAlert update = FrequencyStabilityWindowFunction.advanceIncident(
                tracker, abnormalAssessment(11000L, SeverityLevel.MEDIUM));
        assertEquals(FrequencyIncidentState.UPDATE, update.getIncidentState());
        assertEquals(incidentId, update.getAlertId());

        FrequencyAlert recovery = FrequencyStabilityWindowFunction.advanceIncident(
                tracker, normalAssessment(12000L));
        assertEquals(FrequencyIncidentState.RECOVERY, recovery.getIncidentState());
        assertEquals(incidentId, recovery.getAlertId());

        FrequencyAlert close = FrequencyStabilityWindowFunction.advanceIncident(
                tracker, normalAssessment(17000L));
        assertEquals(FrequencyIncidentState.CLOSE, close.getIncidentState());
        assertEquals(incidentId, close.getAlertId());

        assertNull(FrequencyStabilityWindowFunction.advanceIncident(
                tracker, abnormalAssessment(18000L, SeverityLevel.MEDIUM)));

        FrequencyAlert nextStart = FrequencyStabilityWindowFunction.advanceIncident(
                tracker, abnormalAssessment(27000L, SeverityLevel.MEDIUM));
        assertEquals(FrequencyIncidentState.START, nextStart.getIncidentState());
        assertNotEquals(incidentId, nextStart.getAlertId());
    }

    @Test
    public void emitsAnImmediateUpdateWhenSeverityEscalates() {
        FrequencyStabilityWindowFunction.IncidentTracker tracker =
                new FrequencyStabilityWindowFunction.IncidentTracker();
        FrequencyAlert start = FrequencyStabilityWindowFunction.advanceIncident(
                tracker, abnormalAssessment(1000L, SeverityLevel.MEDIUM));

        FrequencyAlert update = FrequencyStabilityWindowFunction.advanceIncident(
                tracker, abnormalAssessment(2000L, SeverityLevel.CRITICAL));

        assertEquals(FrequencyIncidentState.UPDATE, update.getIncidentState());
        assertEquals(start.getAlertId(), update.getAlertId());
        assertEquals(SeverityLevel.CRITICAL, update.getSeverityLevel());
    }

    @Test
    public void reportsOnlyStatisticallyMeaningfulRegionalDisagreement() {
        List<Measurement> measurements = new ArrayList<>();
        addRegionFrames(measurements, "North", 50.00);
        addRegionFrames(measurements, "South", 50.00);
        addRegionFrames(measurements, "East", 50.01);
        addRegionFrames(measurements, "West", 50.20);

        assertEquals(
                Arrays.asList("West"),
                FrequencyStabilityWindowFunction.findAffectedRegions(measurements)
        );

        measurements.clear();
        addRegionFrames(measurements, "North", 49.50);
        addRegionFrames(measurements, "South", 49.50);
        addRegionFrames(measurements, "East", 49.51);
        addRegionFrames(measurements, "West", 49.49);
        assertEquals(
                new ArrayList<String>(),
                FrequencyStabilityWindowFunction.findAffectedRegions(measurements)
        );

        measurements.clear();
        addRegionFrames(measurements, "North", 49.90);
        addRegionFrames(measurements, "South", 49.90);
        addRegionFrames(measurements, "East", 50.10);
        addRegionFrames(measurements, "West", 50.10);
        assertEquals(
                Arrays.asList("East", "North", "South", "West"),
                FrequencyStabilityWindowFunction.findAffectedRegions(measurements)
        );
        FrequencyStabilityWindowFunction.FrequencyAssessment assessment =
                FrequencyStabilityWindowFunction.assess(measurements, 0L, 3000L);
        assertTrue(assessment.isAbnormal());
        assertEquals(FrequencyAlertType.FREQUENCY_DEVIATION, assessment.alertType);
    }

    private FrequencyStabilityWindowFunction.FrequencyAssessment abnormalAssessment(
            long windowEnd,
            SeverityLevel severityLevel) {
        FrequencyAlertType alertType = severityLevel == SeverityLevel.CRITICAL
                ? FrequencyAlertType.CRITICAL_ROCOF
                : FrequencyAlertType.FREQUENCY_DEVIATION;
        return assessment(windowEnd, alertType, severityLevel, 0.7);
    }

    private FrequencyStabilityWindowFunction.FrequencyAssessment normalAssessment(long windowEnd) {
        return assessment(windowEnd, null, SeverityLevel.LOW, 0.0);
    }

    private FrequencyStabilityWindowFunction.FrequencyAssessment assessment(
            long windowEnd,
            FrequencyAlertType alertType,
            SeverityLevel severityLevel,
            double severityScore) {
        return new FrequencyStabilityWindowFunction.FrequencyAssessment(
                windowEnd - 3000L,
                windowEnd,
                alertType == null ? 50.0 : 49.7,
                49.7,
                50.0,
                alertType == FrequencyAlertType.CRITICAL_ROCOF ? -0.8 : 0.0,
                0.0,
                64,
                alertType,
                severityLevel,
                severityScore,
                new ArrayList<String>()
        );
    }

    private void addRegionFrames(List<Measurement> measurements, String region, double frequency) {
        Measurement first = measurement(1000L, frequency);
        first.setRegion(region);
        measurements.add(first);
        Measurement second = measurement(2000L, frequency);
        second.setRegion(region);
        measurements.add(second);
    }

    private Measurement measurement(long timestamp, double frequency) {
        Measurement measurement = new Measurement();
        measurement.setTimestamp(timestamp);
        measurement.setFrequency(frequency);
        return measurement;
    }

    private FrequencyStabilityWindowFunction.ReportingFrame frame(long timestamp, double frequency) {
        return new FrequencyStabilityWindowFunction.ReportingFrame(timestamp, frequency);
    }
}
