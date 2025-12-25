package org.example.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;



public class Measurement implements Serializable {


    @SerializedName("timestamp")
    @Expose
    private long timestamp;
    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("location")
    @Expose
    private String location;

    @SerializedName("voltage")
    @Expose
    private double voltage;
    @SerializedName("current")
    @Expose
    private double current;
    @SerializedName("frequency")
    @Expose
    private double frequency;

    private final static long serialVersionUID = 5738216193075188632L;


    public Measurement() {}

    public Measurement(
            long timestamp,
            String key,
            String location,
            double voltage,
            double current,
            double frequency
    ) {
        this.timestamp = timestamp;
        this.key = key;
        this.location = location;
        this.voltage = voltage;
        this.current = current;
        this.frequency = frequency;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    @Override
    public String toString() {
        return "{" +
                "timestamp=" + timestamp +
                ", key='" + key + '\'' +
                ", location='" + location + '\'' +
                ", voltage=" + voltage +
                ", current=" + current +
                ", frequency=" + frequency +
                '}';
    }
}
