package com.example.backend.dto;

import com.example.backend.model.FaultAlert;
import com.example.backend.model.enums.AlertType;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FaultAlertDto(String alertId, long timestamp, String pmuId, String region, String substation,
                            String location, String alertType,  String description,
                            double measuredValue, double threshold, double severity, String severityLevel,
                             double voltage, double current, double frequency) {

        public static FaultAlertDto from(FaultAlert faultAlert){
            return new FaultAlertDto(faultAlert.getAlertId(),faultAlert.getTimestamp(), faultAlert.getPmuId(),
                    faultAlert.getRegion(), faultAlert.getSubstation(), faultAlert.getLocation(),
                    faultAlert.getAlertType(),
                    faultAlert.getDescription(), faultAlert.getMeasuredValue(), faultAlert.getThreshold(),
                    faultAlert.getSeverity(), faultAlert.getSeverityLevel(), faultAlert.getVoltage(),
                    faultAlert.getCurrent(), faultAlert.getFrequency());
        }

        public  FaultAlert toFaultAlert(){
            return new FaultAlert(alertId,timestamp,pmuId,region,substation,location,alertType,description,
                    measuredValue,threshold,severity,severityLevel,voltage,current,frequency);
        }

}

