package org.example.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.enums.AlertType;
import org.example.models.enums.SeverityLevel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class FaultAlert implements Serializable {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private String alertId;
    private long timestamp;
    private String pmuId;
    private String region;
    private String substation;
    private String location;
    
    // Alert details (using Enums)
    private AlertType alertType;
    private String description;
    private double measuredValue;
    private double threshold;
    private double severity;              // 0.0 to 1.0
    private SeverityLevel severityLevel;
    
    private double voltage;
    private double current;
    private double frequency;
    
    public static FaultAlert createAlert(
        Measurement measurement,
        AlertType alertType,
        String description,
        double measuredValue,
        double threshold,
        double severity
    ) {
        FaultAlert alert = new FaultAlert();
        
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setTimestamp(measurement.getTimestamp());
        alert.setPmuId(measurement.getPmuId());
        alert.setRegion(measurement.getRegion());
        alert.setSubstation(measurement.getSubstation());
        alert.setLocation(measurement.getLocation());
        
        alert.setAlertType(alertType);
        alert.setDescription(description);
        alert.setMeasuredValue(measuredValue);
        alert.setThreshold(threshold);
        alert.setSeverity(severity);
        alert.setSeverityLevel(SeverityLevel.fromScore(severity));
        
        alert.setVoltage(measurement.getVoltageMagnitude());
        alert.setCurrent(measurement.getCurrentMagnitude());
        alert.setFrequency(measurement.getFrequency());
        
        return alert;
    }

    public FaultAlert() {}
    
    public String getAlertId() { 
        return alertId; 
    }
    
    public void setAlertId(String alertId) { 
        this.alertId = alertId; 
    }
    
    public long getTimestamp() { 
        return timestamp; 
    }
    
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp; 
    }
    
    public String getPmuId() { 
        return pmuId; 
    }
    
    public void setPmuId(String pmuId) { 
        this.pmuId = pmuId; 
    }
    
    public String getRegion() { 
        return region; 
    }
    
    public void setRegion(String region) { 
        this.region = region; 
    }
    
    public String getSubstation() { 
        return substation; 
    }
    
    public void setSubstation(String substation) { 
        this.substation = substation; 
    }
    
    public String getLocation() { 
        return location; 
    }
    
    public void setLocation(String location) { 
        this.location = location; 
    }
    
    public AlertType getAlertType() { 
        return alertType; 
    }
    
    public void setAlertType(AlertType alertType) { 
        this.alertType = alertType; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public double getMeasuredValue() { 
        return measuredValue; 
    }
    
    public void setMeasuredValue(double measuredValue) { 
        this.measuredValue = measuredValue; 
    }
    
    public double getThreshold() { 
        return threshold; 
    }
    
    public void setThreshold(double threshold) { 
        this.threshold = threshold; 
    }
    
    public double getSeverity() { 
        return severity; 
    }
    
    public void setSeverity(double severity) { 
        this.severity = severity; 
    }
    
    public SeverityLevel getSeverityLevel() { 
        return severityLevel; 
    }
    
    public void setSeverityLevel(SeverityLevel severityLevel) { 
        this.severityLevel = severityLevel; 
    }
    
    public double getVoltage() { 
        return voltage; 
    }
    
    public void setVoltage(double voltage) { 
        this.voltage = voltage; 
    }
    
    public double getCurrent() { 
        return current; 
    }
    
    public void setCurrent(double current) { 
        this.current = current; 
    }
    
    public double getFrequency() { 
        return frequency; 
    }
    
    public void setFrequency(double frequency) { 
        this.frequency = frequency; 
    }
    
  
    public String toJson() {
        try {
            Map<String, Object> json = new HashMap<>();
            json.put("alert_id", alertId);
            json.put("timestamp", timestamp);
            json.put("pmu_id", pmuId);
            json.put("region", region);
            json.put("substation", substation);
            json.put("location", location);
            json.put("alert_type", alertType.getDisplayName());
            json.put("description", description);
            json.put("measured_value", measuredValue);
            json.put("threshold", threshold);
            json.put("severity", severity);
            json.put("severity_level", severityLevel.getDisplayName());
            json.put("voltage", voltage);
            json.put("current", current);
            json.put("frequency", frequency); 
            return mapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
    
    @Override
    public String toString() {
        return String.format("FaultAlert[pmu=%s, type=%s, value=%.2f, severity=%s]",
            pmuId, alertType.name(), measuredValue, severityLevel.name());
    }


}