package com.example.backend.dto;

import com.example.backend.model.FrequencyAlert;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.FrequencyIncidentState;
import com.example.backend.model.enums.SeverityLevel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

public record FrequencyAlertDto(String alertId, long timestamp, long windowStart, long windowEnd, String region,
                                String incidentState, List<String> affectedRegions,
                                double avgFrequency, double minFrequency, double maxFrequency, double frequencyDeviation,
                                double rocof, double rocofVolatility,  String alertDisplayName,String alertDescription,
                                String message, String severityLevel, double severityScore, int measurementCount) {

    public static FrequencyAlertDto from(FrequencyAlert frequencyAlert){
        return new FrequencyAlertDto(frequencyAlert.getAlertId(), frequencyAlert.getTimestamp(),
                frequencyAlert.getWindowStart(), frequencyAlert.getWindowEnd(), frequencyAlert.getRegion(),
                frequencyAlert.getIncidentState() != null ? frequencyAlert.getIncidentState().name() : null,
                decodeAffectedRegions(frequencyAlert.getAffectedRegions()),
                frequencyAlert.getAvgFrequency(), frequencyAlert.getMinFrequency(), frequencyAlert.getMaxFrequency(),
                frequencyAlert.getFrequencyDeviation(), frequencyAlert.getRocof(), frequencyAlert.getRocofVolatility(),
                frequencyAlert.getAlertType().getDisplayName(), frequencyAlert.getAlertDescription(),
                frequencyAlert.getMessage(), frequencyAlert.getSeverityLevel().getDisplayName(),
                frequencyAlert.getSeverityScore(), frequencyAlert.getMeasurementCount());
    }

    public FrequencyAlert toEntity(){

        return new FrequencyAlert(alertId, windowStart, windowEnd, timestamp, region,
                incidentState != null ? FrequencyIncidentState.valueOf(incidentState) : null,
                encodeAffectedRegions(affectedRegions), avgFrequency, minFrequency, maxFrequency,
                frequencyDeviation, rocof, rocofVolatility, FrequencyAlertType.fromDisplayName(alertDisplayName),
                alertDescription, message, SeverityLevel.fromDisplayName(severityLevel), severityScore, measurementCount);
    }

    private static String encodeAffectedRegions(List<String> affectedRegions) {
        return affectedRegions == null ? "" : String.join(",", affectedRegions);
    }

    private static List<String> decodeAffectedRegions(String affectedRegions) {
        if (affectedRegions == null || affectedRegions.isBlank()) return Collections.emptyList();
        return Arrays.asList(affectedRegions.split(","));
    }
}
