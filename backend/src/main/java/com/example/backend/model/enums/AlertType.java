package com.example.backend.model.enums;

public enum AlertType {
    VOLTAGE_SAG("Voltage Sag", "Voltage dropped below safe threshold"),
    VOLTAGE_SWELL("Voltage Swell", "Voltage exceeded upper safe threshold"),
    OVERCURRENT("Overcurrent", "Current exceeded safe operating limit");

    private final String displayName;
    private final String description;

    AlertType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
