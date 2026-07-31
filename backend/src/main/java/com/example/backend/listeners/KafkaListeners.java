package com.example.backend.listeners;

import com.example.backend.dto.FaultAlertDto;
import com.example.backend.dto.FrequencyAlertDto;
import com.example.backend.dto.SystemMetricsDto;
import com.example.backend.service.application.FaultAlertApplicationService;
import com.example.backend.service.application.FrequencyAlertApplicationService;
import com.example.backend.service.application.SystemMetricsApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    private final FaultAlertApplicationService faultAlertApplicationService;
    private final FrequencyAlertApplicationService frequencyAlertApplicationService;
    private final SystemMetricsApplicationService systemMetricsApplicationService;
    private static final ObjectMapper objectMapper=new ObjectMapper();


    public KafkaListeners(FaultAlertApplicationService faultAlertApplicationService,
                          FrequencyAlertApplicationService frequencyAlertApplicationService,
                          SystemMetricsApplicationService systemMetricsApplicationService) {
        this.faultAlertApplicationService = faultAlertApplicationService;
        this.frequencyAlertApplicationService = frequencyAlertApplicationService;
        this.systemMetricsApplicationService = systemMetricsApplicationService;
    }

    @KafkaListener(topics = "system-metrics")
    public void onMetric(String json) throws JsonProcessingException {
        try {
            systemMetricsApplicationService.save(objectMapper.readValue(json, SystemMetricsDto.class));
        } catch (Exception e) {
            System.err.println("Failed to deserialize: " + json);
            throw e;
        }
    }

    @KafkaListener(topics = "fault-alerts")
    public void onFaultAlert(String json) throws JsonProcessingException {
        try {
            faultAlertApplicationService.save(objectMapper.readValue(json, FaultAlertDto.class));
        } catch (Exception e) {
            System.err.println("Failed to deserialize: " + json);
            throw e;
        }
    }

    @KafkaListener(topics = "frequency-alerts")
    public void onFrequencyAlert(String json) throws JsonProcessingException {
        try {
            frequencyAlertApplicationService.save(objectMapper.readValue(json, FrequencyAlertDto.class));
        } catch (Exception e) {
            System.err.println("Failed to deserialize: " + json);
            throw e;
        }
    }
}
