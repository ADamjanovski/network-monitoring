package org.example.models;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AnomalyResult implements Serializable
{

    @SerializedName("pmuId")
    @Expose
    private String pmuId;

    @SerializedName("anomaly_type")
    @Expose
    private String anomalyType;
    @SerializedName("window_start")
    @Expose
    private Long windowStart;
    @SerializedName("window_end")
    @Expose
    private Long windowEnd;
    @SerializedName("measured_value")
    @Expose
    private Double measuredValue;
    @SerializedName("threshold_boundary")
    @Expose
    private Double thresholdBoundary;
    @SerializedName("severity_score")
    @Expose
    private Double severityScore;
    private final static long serialVersionUID = 6205437282762047004L;

    /**
     * No args constructor for use in serialization
     *
     */
    public AnomalyResult() {
    }

    public AnomalyResult(String pmuId,String anomalyType, Long windowStart, Long windowEnd, Double measuredValue, Double thresholdBoundary) {
        super();
        this.pmuId = pmuId;
        this.anomalyType=anomalyType;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.measuredValue = measuredValue;
        this.thresholdBoundary = thresholdBoundary;
        this.severityScore = calculateSeverityScore(measuredValue,thresholdBoundary);
    }

    public String getPmuId() {
        return pmuId;
    }

    public void setPmuId(String pmuId) {
        this.pmuId = pmuId;
    }

    public Long getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Long windowStart) {
        this.windowStart = windowStart;
    }

    public Long getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(Long windowEnd) {
        this.windowEnd = windowEnd;
    }

    public Double getMeasuredValue() {
        return measuredValue;
    }

    public void setMeasuredValue(Double measuredValue) {
        this.measuredValue = measuredValue;
    }

    public Double getThresholdBoundary() {
        return thresholdBoundary;
    }

    public void setThresholdBoundary(Double thresholdBoundary) {
        this.thresholdBoundary = thresholdBoundary;
    }

    public Double getSeverityScore() {
        return severityScore;
    }

    public void setSeverityScore(Double severityScore) {
        this.severityScore = severityScore;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this,AnomalyResult.class);
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.windowStart == null)? 0 :this.windowStart.hashCode()));
        result = ((result* 31)+((this.thresholdBoundary == null)? 0 :this.thresholdBoundary.hashCode()));
        result = ((result* 31)+((this.windowEnd == null)? 0 :this.windowEnd.hashCode()));
        result = ((result* 31)+((this.severityScore == null)? 0 :this.severityScore.hashCode()));
        result = ((result* 31)+((this.pmuId == null)? 0 :this.pmuId.hashCode()));
        result = ((result* 31)+((this.measuredValue == null)? 0 :this.measuredValue.hashCode()));
//        result = ((result* 31)+((this.anomalyType == null)? 0 :this.anomalyType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AnomalyResult) == false) {
            return false;
        }
        AnomalyResult rhs = ((AnomalyResult) other);
        return (((((((this.windowStart == rhs.windowStart)||((this.windowStart!= null)&&this.windowStart.equals(rhs.windowStart)))&&((this.thresholdBoundary == rhs.thresholdBoundary)||((this.thresholdBoundary!= null)&&this.thresholdBoundary.equals(rhs.thresholdBoundary))))&&((this.windowEnd == rhs.windowEnd)||((this.windowEnd!= null)&&this.windowEnd.equals(rhs.windowEnd))))&&((this.severityScore == rhs.severityScore)||((this.severityScore!= null)&&this.severityScore.equals(rhs.severityScore))))&&((this.pmuId == rhs.pmuId)||((this.pmuId!= null)&&this.pmuId.equals(rhs.pmuId))))&&((this.measuredValue == rhs.measuredValue)||((this.measuredValue!= null)&&this.measuredValue.equals(rhs.measuredValue))));
    }


    private double calculateSeverityScore(double measuredValue, double threshold) {
        if (threshold == 0) {
            return 0;
        }
        double deviation = Math.abs(measuredValue - threshold);
        double percentage = (deviation / threshold) * 100.0;
        return Math.round(percentage * 100.0) / 100.0;
    }
}
