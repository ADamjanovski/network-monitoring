package com.example.backend.dto;

import com.example.backend.model.SystemMetrics;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SystemMetricsDto(long timestamp, long windowStart, long windowEnd, int activePmuCount, double avgFrequency,
                               double minFrequency, double maxFrequency, double avgVoltage, double minVoltage,
                               double maxVoltage, double avgCurrent, double minCurrent, double maxCurrent) {

    public static SystemMetricsDto from(SystemMetrics systemMetrics){
        return new SystemMetricsDto(systemMetrics.getTimestamp(), systemMetrics.getWindowStart(), systemMetrics.getWindowEnd(),
                systemMetrics.getActivePmuCount(), systemMetrics.getAvgFrequency(), systemMetrics.getMinFrequency(),
                systemMetrics.getMaxFrequency(), systemMetrics.getAvgVoltage(), systemMetrics.getMinVoltage(),
                systemMetrics.getMaxVoltage(), systemMetrics.getAvgCurrent(), systemMetrics.getMinCurrent(),
                systemMetrics.getMaxCurrent());
    }

    public SystemMetrics toEntity(){
        return new SystemMetrics(timestamp, windowStart, windowEnd, activePmuCount, avgFrequency, minFrequency,
                maxFrequency, avgVoltage, minVoltage, maxVoltage, avgCurrent, minCurrent, maxCurrent);
    }
}
