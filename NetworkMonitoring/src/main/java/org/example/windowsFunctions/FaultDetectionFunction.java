package org.example.windowsFunctions;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.example.models.Measurement;
import org.example.models.FaultAlert;
import org.example.models.AlertType;
import org.example.models.SeverityLevel;

import java.util.UUID;

public class FaultDetectionFunction implements FlatMapFunction<Measurement, FaultAlert> {
    
    private static final double NOMINAL_VOLTAGE = 20000.0;  // 20 kV
    private static final double NOMINAL_CURRENT = 400.0;    // 400 A
    
    private static final double VOLTAGE_SAG_THRESHOLD = 18000.0;   // 90% (18 kV)
    private static final double VOLTAGE_SWELL_THRESHOLD = 22000.0; // 110% (22 kV)
    private static final double OVERCURRENT_THRESHOLD = 1200.0;    // 300% (1200 A)
    
    @Override
    public void flatMap(Measurement measurement, Collector<FaultAlert> out) {
        
        double voltage = measurement.getVoltageMagnitude();
        double current = measurement.getCurrentMagnitude();
        
        // Check VOLTAGE_SAG
        if (voltage < VOLTAGE_SAG_THRESHOLD) {
            double severity = calculateVoltageSeverity(voltage, VOLTAGE_SAG_THRESHOLD, true);
            
            FaultAlert alert = FaultAlert.createAlert(
                measurement,
                AlertType.VOLTAGE_SAG,
                String.format("Voltage dropped to %.2f kV (threshold: %.2f kV)", 
                    voltage / 1000.0, VOLTAGE_SAG_THRESHOLD / 1000.0),
                voltage,
                VOLTAGE_SAG_THRESHOLD,
                severity
            );
            
            out.collect(alert);
        }
        
        // Check VOLTAGE_SWELL
        if (voltage > VOLTAGE_SWELL_THRESHOLD) {
            double severity = calculateVoltageSeverity(voltage, VOLTAGE_SWELL_THRESHOLD, false);
            
            FaultAlert alert = FaultAlert.createAlert(
                measurement,
                AlertType.VOLTAGE_SWELL,
                String.format("Voltage increased to %.2f kV (threshold: %.2f kV)", 
                    voltage / 1000.0, VOLTAGE_SWELL_THRESHOLD / 1000.0),
                voltage,
                VOLTAGE_SWELL_THRESHOLD,
                severity
            );
            
            out.collect(alert);
        }
        
        // Check OVERCURRENT 
        if (current > OVERCURRENT_THRESHOLD) {
            double severity = calculateCurrentSeverity(current, OVERCURRENT_THRESHOLD);
            
            FaultAlert alert = FaultAlert.createAlert(
                measurement,
                AlertType.OVERCURRENT,
                String.format("Current exceeded threshold: %.2f A (threshold: %.2f A)", 
                    current, OVERCURRENT_THRESHOLD),
                current,
                OVERCURRENT_THRESHOLD,
                severity
            );
            
            out.collect(alert);
        }
        
    }
    
    private double calculateVoltageSeverity(double voltage, double threshold, boolean isSag) {
        double deviation;
        
        if (isSag) {
            // How far below threshold (percentage of nominal)
            deviation = (threshold - voltage) / NOMINAL_VOLTAGE;
        } else {
            // How far above threshold (percentage of nominal)
            deviation = (voltage - threshold) / NOMINAL_VOLTAGE;
        }
        
        // Normalize: 10% deviation = severity 1.0
        return Math.min(1.0, deviation / 0.10);
    }
    
    private double calculateCurrentSeverity(double current, double threshold) {
        // How many times over threshold
        double multiplier = current / threshold;
        
        // Normalize: 1x over threshold = 0.0, 4x over = 1.0
        return Math.min(1.0, (multiplier - 1.0) / 3.0);
    }
}
