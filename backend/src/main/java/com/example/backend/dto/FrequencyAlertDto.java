package com.example.backend.dto;

import com.example.backend.model.FrequencyAlert;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

public record FrequencyAlertDto(String alertId, long timestamp, long windowStart, long windowEnd,  String region,
                                double avgFrequency, double minFrequency, double maxFrequency, double frequencyDeviation,
                                double rocof, double rocofVolatility,  String alertDisplayName,String alertDescription,
                                String message, String severityLevel, double severityScore, int measurementCount) {

    public static FrequencyAlertDto from(FrequencyAlert frequencyAlert){
        return new FrequencyAlertDto(frequencyAlert.getAlertId(), frequencyAlert.getTimestamp(),
                frequencyAlert.getWindowStart(), frequencyAlert.getWindowEnd(), frequencyAlert.getRegion(),
                frequencyAlert.getAvgFrequency(), frequencyAlert.getMinFrequency(), frequencyAlert.getMaxFrequency(),
                frequencyAlert.getFrequencyDeviation(), frequencyAlert.getRocof(), frequencyAlert.getRocofVolatility(),
                frequencyAlert.getAlertType().getDisplayName(), frequencyAlert.getAlertDescription(),
                frequencyAlert.getMessage(), frequencyAlert.getSeverityLevel().getDisplayName(),
                frequencyAlert.getSeverityScore(), frequencyAlert.getMeasurementCount());
    }

    public FrequencyAlert toEntity(){

        return new FrequencyAlert(alertId,windowStart,windowEnd,timestamp,region,avgFrequency,minFrequency,maxFrequency,
                frequencyDeviation, rocof, rocofVolatility, FrequencyAlertType.fromDisplayName(alertDisplayName),
                alertDescription, message, SeverityLevel.fromDisplayName(severityLevel), severityScore, measurementCount);
    }
}
