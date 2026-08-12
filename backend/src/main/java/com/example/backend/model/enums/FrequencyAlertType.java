package com.example.backend.model.enums;

public enum FrequencyAlertType {
    FREQUENCY_DEVIATION("Frequency Deviation",
            "Frequency moved outside safe operating range (49.8 - 50.2 Hz)"),

    HIGH_ROCOF("High Rate of Change of Frequency",
            "RoCoF exceeded warning threshold (±0.5 Hz/s) - possible generator trip"),

    CRITICAL_ROCOF("Critical Rate of Change of Frequency",
            "RoCoF exceeded critical threshold (±1.0 Hz/s) - imminent cascade risk");

    private final String displayName;
    private final String description;

    FrequencyAlertType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public static FrequencyAlertType fromDisplayName(String displayName) {
        for (FrequencyAlertType alertType : values()) {
            if (alertType.displayName.equals(displayName)) {
                return alertType;
            }
        }
        throw new IllegalArgumentException("Unknown frequency alert type: " + displayName);
    }
}
