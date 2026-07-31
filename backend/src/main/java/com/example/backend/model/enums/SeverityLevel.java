package com.example.backend.model.enums;

public enum SeverityLevel {
    LOW(0.0, 0.3, "Low"),
    MEDIUM(0.3, 0.5, "Medium"),
    HIGH(0.5, 0.8, "High"),
    CRITICAL(0.8, 1.0, "Critical");

    private final double minSeverity;
    private final double maxSeverity;
    private final String displayName;

    SeverityLevel(double minSeverity, double maxSeverity, String displayName) {
        this.minSeverity = minSeverity;
        this.maxSeverity = maxSeverity;
        this.displayName = displayName;
    }

    public double getMinSeverity() {
        return minSeverity;
    }

    public double getMaxSeverity() {
        return maxSeverity;
    }

    public String getDisplayName() {
        return displayName;
    }


    public static SeverityLevel fromScore(double severity) {
        if (severity >= CRITICAL.minSeverity) return CRITICAL;
        if (severity >= HIGH.minSeverity) return HIGH;
        if (severity >= MEDIUM.minSeverity) return MEDIUM;
        return LOW;
    }
}
