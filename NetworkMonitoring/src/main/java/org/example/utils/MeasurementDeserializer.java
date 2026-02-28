package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;
import org.example.models.Measurement;


public class MeasurementDeserializer implements MapFunction<String, Measurement> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public Measurement map(String json) throws Exception {
        try {
            return mapper.readValue(json, Measurement.class);
        } catch (Exception e) {
            System.err.println("Failed to deserialize: " + json);
            throw e;
        }
    }
}