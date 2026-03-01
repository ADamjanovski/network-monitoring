package org.example.windowsFunctions;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.example.models.Measurement;
import org.example.models.FaultAlert;
import org.example.models.AlertType;
import org.example.models.SeverityLevel;
import org.example.models.configuration.AppConfig;

import java.util.UUID;

public class FaultDetectionFunction implements FlatMapFunction<Measurement, FaultAlert> {

    @Override
    public void flatMap(Measurement measurement, Collector<FaultAlert> out) {

        double voltage = measurement.getVoltageMagnitude();
        double current = measurement.getCurrentMagnitude();

        if (voltage < AppConfig.VOLTAGE_SAG_THRESHOLD) {
            out.collect(FaultAlert.createAlert(
                measurement,
                AlertType.VOLTAGE_SAG,
                String.format("Voltage dropped to %.2f kV (threshold: %.2f kV)",
                    voltage / 1000.0, AppConfig.VOLTAGE_SAG_THRESHOLD / 1000.0),
                voltage,
                AppConfig.VOLTAGE_SAG_THRESHOLD,
                calculateVoltageSeverity(voltage, AppConfig.VOLTAGE_SAG_THRESHOLD, true)
            ));
        }

        if (voltage > AppConfig.VOLTAGE_SWELL_THRESHOLD) {
            out.collect(FaultAlert.createAlert(
                measurement,
                AlertType.VOLTAGE_SWELL,
                String.format("Voltage increased to %.2f kV (threshold: %.2f kV)",
                    voltage / 1000.0, AppConfig.VOLTAGE_SWELL_THRESHOLD / 1000.0),
                voltage,
                AppConfig.VOLTAGE_SWELL_THRESHOLD,
                calculateVoltageSeverity(voltage, AppConfig.VOLTAGE_SWELL_THRESHOLD, false)
            ));
        }

        if (current > AppConfig.OVERCURRENT_THRESHOLD) {
            out.collect(FaultAlert.createAlert(
                measurement,
                AlertType.OVERCURRENT,
                String.format("Current exceeded threshold: %.2f A (threshold: %.2f A)",
                    current, AppConfig.OVERCURRENT_THRESHOLD),
                current,
                AppConfig.OVERCURRENT_THRESHOLD,
                calculateCurrentSeverity(current, AppConfig.OVERCURRENT_THRESHOLD)
            ));
        }
    }

    private double calculateVoltageSeverity(double voltage, double threshold, boolean isSag) {
        double deviation = isSag
            ? (threshold - voltage) / AppConfig.NOMINAL_VOLTAGE
            : (voltage - threshold) / AppConfig.NOMINAL_VOLTAGE;
        return Math.min(1.0, deviation / 0.10);
    }

    private double calculateCurrentSeverity(double current, double threshold) {
        return Math.min(1.0, (current / threshold - 1.0) / 3.0);
    }
}
