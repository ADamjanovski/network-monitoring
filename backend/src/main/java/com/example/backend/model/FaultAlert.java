package com.example.backend.model;

import com.example.backend.model.enums.AlertType;
import com.example.backend.model.enums.SeverityLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FaultAlert {
    @Id
    private String alertId;
    private long timestamp;
    private String pmuId;
    private String region;
    private String substation;
    private String location;

    // Alert details (using Enums)
    private String alertType;
    private String description;
    private double measuredValue;
    private double threshold;
    private double severity;              // 0.0 to 1.0
    private String severityLevel;

    private double voltage;
    private double current;
    private double frequency;
}
