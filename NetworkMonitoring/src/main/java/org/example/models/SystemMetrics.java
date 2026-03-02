package org.example.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.Serializable;

public class SystemMetrics implements Serializable {

    private static final ObjectMapper mapper = new ObjectMapper();

    private long timestamp;
    private long windowStart;
    private long windowEnd;

    private int activePmuCount;

    private double avgFrequency;
    private double minFrequency;
    private double maxFrequency;

    private double avgVoltage;
    private double minVoltage;
    private double maxVoltage;

    private double avgCurrent;
    private double minCurrent;
    private double maxCurrent;

    public SystemMetrics() {}

    @Override
    public String toString() {
        return String.format(
                "SYSTEM | PMUs: %d | Freq: %.3f (MIN:%.3f-MAX:%.3f) | Volt: %.1f (MIN:%.1f-MAX:%.1f) | Curr: %.1f",
                activePmuCount,
                avgFrequency, minFrequency, maxFrequency,
                avgVoltage,   minVoltage,   maxVoltage,
                avgCurrent
        );
    }

    public String toJson() {
        try {
            ObjectNode node = mapper.createObjectNode();
            node.put("timestamp", timestamp);
            node.put("window_start", windowStart);
            node.put("window_end", windowEnd);
            node.put("active_pmu_count", activePmuCount);
            node.put("avg_frequency", avgFrequency);
            node.put("min_frequency", minFrequency);
            node.put("max_frequency", maxFrequency);
            node.put("avg_voltage", avgVoltage);
            node.put("min_voltage", minVoltage);
            node.put("max_voltage", maxVoltage);
            node.put("avg_current", avgCurrent);
            node.put("min_current", minCurrent);
            node.put("max_current", maxCurrent);
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{\"error\": \"serialization failed\"}";
        }
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getWindowStart() { return windowStart; }
    public void setWindowStart(long windowStart) { this.windowStart = windowStart; }

    public long getWindowEnd() { return windowEnd; }
    public void setWindowEnd(long windowEnd) { this.windowEnd = windowEnd; }

    public int getActivePmuCount() { return activePmuCount; }
    public void setActivePmuCount(int activePmuCount) { this.activePmuCount = activePmuCount; }

    public double getAvgFrequency() { return avgFrequency; }
    public void setAvgFrequency(double avgFrequency) { this.avgFrequency = avgFrequency; }

    public double getMinFrequency() { return minFrequency; }
    public void setMinFrequency(double minFrequency) { this.minFrequency = minFrequency; }

    public double getMaxFrequency() { return maxFrequency; }
    public void setMaxFrequency(double maxFrequency) { this.maxFrequency = maxFrequency; }

    public double getAvgVoltage() { return avgVoltage; }
    public void setAvgVoltage(double avgVoltage) { this.avgVoltage = avgVoltage; }

    public double getMinVoltage() { return minVoltage; }
    public void setMinVoltage(double minVoltage) { this.minVoltage = minVoltage; }

    public double getMaxVoltage() { return maxVoltage; }
    public void setMaxVoltage(double maxVoltage) { this.maxVoltage = maxVoltage; }

    public double getAvgCurrent() { return avgCurrent; }
    public void setAvgCurrent(double avgCurrent) { this.avgCurrent = avgCurrent; }

    public double getMinCurrent() { return minCurrent; }
    public void setMinCurrent(double minCurrent) { this.minCurrent = minCurrent; }

    public double getMaxCurrent() { return maxCurrent; }
    public void setMaxCurrent(double maxCurrent) { this.maxCurrent = maxCurrent; }
}