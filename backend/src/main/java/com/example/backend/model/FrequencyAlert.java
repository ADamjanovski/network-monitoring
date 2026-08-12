package com.example.backend.model;

import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FrequencyAlert {

    @Id
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

    @Enumerated(EnumType.STRING)
    private FrequencyAlertType alertType;
    private String alertDescription;
    private String message;
    @Enumerated(EnumType.STRING)
    private SeverityLevel severityLevel;
    private double severityScore;

    private int measurementCount;
}
