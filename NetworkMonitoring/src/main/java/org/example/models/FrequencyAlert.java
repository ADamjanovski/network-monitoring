package org.example.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.models.enums.FrequencyAlertType;
import org.example.models.enums.SeverityLevel;

import java.io.Serializable;

public class FrequencyAlert implements Serializable {

    private static final ObjectMapper mapper = new ObjectMapper();

    private String alertId;
    private long windowStart;
    private long windowEnd;
    private long timestamp;
    private String region;

    private double avgFrequency;
    private double minFrequency;
    private double maxFrequency;
    private double frequencyDeviation;
    private double rocof;
    private double rocofVolatility;

    private FrequencyAlertType alertType;
    private String message;
    private SeverityLevel severityLevel;
    private double severityScore;

    private int measurementCount;

    public FrequencyAlert() {}

    @Override
    public String toString() {
        return String.format("FrequencyAlert[region=%s, type=%s, avg=%.4fHz, dev=%+.4f, rocof=%+.4f, severity=%s, score=%.2f]",
                region,
                alertType != null ? alertType.name() : "UNKNOWN",
                avgFrequency,
                frequencyDeviation,
                rocof,
                severityLevel != null ? severityLevel.name() : "INFO",
                severityScore);
    }

    public String toJson() {
        try {
            ObjectNode node = mapper.createObjectNode();
            node.put("alert_id", alertId);
            node.put("timestamp", timestamp);
            node.put("window_start", windowStart);
            node.put("window_end", windowEnd);
            node.put("region", region);
            node.put("avg_frequency", avgFrequency);
            node.put("min_frequency", minFrequency);
            node.put("max_frequency", maxFrequency);
            node.put("frequency_deviation", frequencyDeviation);
            node.put("rocof", rocof);
            node.put("rocof_volatility", rocofVolatility);
            node.put("measurement_count", measurementCount);
            node.put("alert_type", alertType != null ? alertType.name() : null);
            node.put("alert_display_name", alertType != null ? alertType.getDisplayName() : null);
            node.put("message", message);
            node.put("severity_score", severityScore);
            node.put("severity_level", severityLevel != null ? severityLevel.getDisplayName() : null);
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{\"error\": \"serialization failed\"}";
        }
    }

    // Getters & Setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getWindowStart() { return windowStart; }
    public void setWindowStart(long windowStart) { this.windowStart = windowStart; }

    public long getWindowEnd() { return windowEnd; }
    public void setWindowEnd(long windowEnd) { this.windowEnd = windowEnd; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public double getAvgFrequency() { return avgFrequency; }
    public void setAvgFrequency(double avgFrequency) { this.avgFrequency = avgFrequency; }

    public double getMinFrequency() { return minFrequency; }
    public void setMinFrequency(double minFrequency) { this.minFrequency = minFrequency; }

    public double getMaxFrequency() { return maxFrequency; }
    public void setMaxFrequency(double maxFrequency) { this.maxFrequency = maxFrequency; }

    public double getFrequencyDeviation() { return frequencyDeviation; }
    public void setFrequencyDeviation(double frequencyDeviation) { this.frequencyDeviation = frequencyDeviation; }

    public double getRocof() { return rocof; }
    public void setRocof(double rocof) { this.rocof = rocof; }

    public double getRocofVolatility() { return rocofVolatility; }
    public void setRocofVolatility(double rocofVolatility) { this.rocofVolatility = rocofVolatility; }

    public FrequencyAlertType getAlertType() { return alertType; }
    public void setAlertType(FrequencyAlertType alertType) { this.alertType = alertType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public SeverityLevel getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(SeverityLevel severityLevel) { this.severityLevel = severityLevel; }

    public double getSeverityScore() { return severityScore; }
    public void setSeverityScore(double severityScore) { this.severityScore = severityScore; }

    public int getMeasurementCount() { return measurementCount; }
    public void setMeasurementCount(int measurementCount) { this.measurementCount = measurementCount; }
}