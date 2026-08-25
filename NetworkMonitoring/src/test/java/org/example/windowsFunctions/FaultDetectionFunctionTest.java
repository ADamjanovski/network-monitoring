package org.example.windowsFunctions;

import org.apache.flink.util.Collector;
import org.example.models.FaultAlert;
import org.example.models.Measurement;
import org.example.models.enums.AlertType;
import org.example.models.enums.SeverityLevel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FaultDetectionFunctionTest {

    private final FaultDetectionFunction detector = new FaultDetectionFunction();

    @Test
    public void overcurrentSeverityStartsLowAtThresholdAndSpansAllLevels() throws Exception {
        assertAlert(1201.0, AlertType.OVERCURRENT, SeverityLevel.LOW, 0.00125);
        assertAlert(1440.0, AlertType.OVERCURRENT, SeverityLevel.MEDIUM, 0.30);
        assertAlert(1600.0, AlertType.OVERCURRENT, SeverityLevel.HIGH, 0.50);
        assertAlert(1840.0, AlertType.OVERCURRENT, SeverityLevel.CRITICAL, 0.80);
        assertAlert(2200.0, AlertType.OVERCURRENT, SeverityLevel.CRITICAL, 1.0);
    }

    @Test
    public void voltageSagSeveritySpansAllLevels() throws Exception {
        assertVoltageAlert(17900.0, AlertType.VOLTAGE_SAG, SeverityLevel.LOW, 0.05);
        assertVoltageAlert(17200.0, AlertType.VOLTAGE_SAG, SeverityLevel.MEDIUM, 0.40);
        assertVoltageAlert(16700.0, AlertType.VOLTAGE_SAG, SeverityLevel.HIGH, 0.65);
        assertVoltageAlert(16200.0, AlertType.VOLTAGE_SAG, SeverityLevel.CRITICAL, 0.90);
    }

    @Test
    public void voltageSwellSeveritySpansAllLevels() throws Exception {
        assertVoltageAlert(22100.0, AlertType.VOLTAGE_SWELL, SeverityLevel.LOW, 0.05);
        assertVoltageAlert(22800.0, AlertType.VOLTAGE_SWELL, SeverityLevel.MEDIUM, 0.40);
        assertVoltageAlert(23300.0, AlertType.VOLTAGE_SWELL, SeverityLevel.HIGH, 0.65);
        assertVoltageAlert(23800.0, AlertType.VOLTAGE_SWELL, SeverityLevel.CRITICAL, 0.90);
    }

    private void assertAlert(
            double current,
            AlertType expectedType,
            SeverityLevel expectedLevel,
            double expectedSeverity
    ) throws Exception {
        Measurement measurement = normalMeasurement();
        measurement.setCurrentMagnitude(current);
        FaultAlert alert = detectSingleAlert(measurement);

        assertEquals(expectedType, alert.getAlertType());
        assertEquals(expectedLevel, alert.getSeverityLevel());
        assertEquals(expectedSeverity, alert.getSeverity(), 0.000001);
    }

    private void assertVoltageAlert(
            double voltage,
            AlertType expectedType,
            SeverityLevel expectedLevel,
            double expectedSeverity
    ) throws Exception {
        Measurement measurement = normalMeasurement();
        measurement.setVoltageMagnitude(voltage);
        FaultAlert alert = detectSingleAlert(measurement);

        assertEquals(expectedType, alert.getAlertType());
        assertEquals(expectedLevel, alert.getSeverityLevel());
        assertEquals(expectedSeverity, alert.getSeverity(), 0.000001);
    }

    private FaultAlert detectSingleAlert(Measurement measurement) throws Exception {
        List<FaultAlert> alerts = new ArrayList<>();
        detector.flatMap(measurement, new Collector<FaultAlert>() {
            @Override
            public void collect(FaultAlert alert) {
                alerts.add(alert);
            }

            @Override
            public void close() {
            }
        });
        assertEquals(1, alerts.size());
        return alerts.get(0);
    }

    private Measurement normalMeasurement() {
        Measurement measurement = new Measurement();
        measurement.setVoltageMagnitude(20000.0);
        measurement.setCurrentMagnitude(400.0);
        measurement.setFrequency(50.0);
        return measurement;
    }
}
