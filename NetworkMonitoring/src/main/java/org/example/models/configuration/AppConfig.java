package org.example.models.configuration;

public final class AppConfig {

    private AppConfig() {}

    // Kafka
    public static final String KAFKA_BOOTSTRAP_SERVERS =
            System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    public static final String INPUT_TOPIC             = "pmu-measurements";
    public static final String FAULT_ALERTS_TOPIC      = "fault-alerts";
    public static final String FREQUENCY_ALERTS_TOPIC  = "frequency-alerts";
    public static final String SYSTEM_METRICS_TOPIC    = "system-metrics";

    // Electrical nominal values
    public static final double NOMINAL_VOLTAGE   = 20000.0; // 20 kV
    public static final double NOMINAL_CURRENT   = 400.0;   // 400 A
    public static final double NOMINAL_FREQUENCY = 50.0;    // Hz

    // Fault Detection thresholds (App 1)
    public static final double VOLTAGE_SAG_THRESHOLD    = 18000.0; // 90%
    public static final double VOLTAGE_SWELL_THRESHOLD  = 22000.0; // 110%
    public static final double OVERCURRENT_THRESHOLD    = 1200.0;  // 300%

    // Frequency thresholds (App 2)
    public static final double FREQ_WARNING_THRESHOLD   = 0.2;
    public static final double ROCOF_WARNING_THRESHOLD  = 0.33;
    public static final double ROCOF_CRITICAL_THRESHOLD = 0.67;

    // System Health thresholds (App 3)
    public static final double VOLTAGE_WARNING_DEV  = 1000.0; // ±5%
    public static final double VOLTAGE_CRITICAL_DEV = 2000.0; // ±10%
    public static final double CURRENT_WARNING      = 600.0;  // 150%
    public static final double CURRENT_CRITICAL     = 1000.0; // 250%
    public static final double FREQ_CRITICAL_DEV    = 0.5;    // ±0.5 Hz
}
