package org.example.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;


public class Measurement implements Serializable {
    
    private long timestamp;
    
    @JsonProperty("pmu_id")
    private String pmuId;
    
    private String location;
    private String substation;
    private String region;
    
    @JsonProperty("voltage_magnitude")
    private double voltageMagnitude;
    
    @JsonProperty("current_magnitude")
    private double currentMagnitude;
    
    private double frequency;
    
    @JsonProperty("voltage_level")
    private String voltageLevel;
    
    public Measurement() {}
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getPmuId() { return pmuId; }
    public void setPmuId(String pmuId) { this.pmuId = pmuId; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getSubstation() { return substation; }
    public void setSubstation(String substation) { this.substation = substation; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public double getVoltageMagnitude() { return voltageMagnitude; }
    public void setVoltageMagnitude(double voltageMagnitude) { 
        this.voltageMagnitude = voltageMagnitude; 
    }
    
    public double getCurrentMagnitude() { return currentMagnitude; }
    public void setCurrentMagnitude(double currentMagnitude) { 
        this.currentMagnitude = currentMagnitude; 
    }
    
    public double getFrequency() { return frequency; }
    public void setFrequency(double frequency) { this.frequency = frequency; }
    
    public String getVoltageLevel() { return voltageLevel; }
    public void setVoltageLevel(String voltageLevel) { this.voltageLevel = voltageLevel; }
    
    @Override
    public String toString() {
        return String.format("Measurement[pmu=%s, V=%.2f, I=%.2f, f=%.4f]", 
            pmuId, voltageMagnitude, currentMagnitude, frequency);
    }
}