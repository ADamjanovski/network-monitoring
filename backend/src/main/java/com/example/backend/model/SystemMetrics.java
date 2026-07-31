package com.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SystemMetrics {
    @Id
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

}
