package com.example.backend.listeners;

import com.example.backend.dto.FaultAlertDto;
import com.example.backend.dto.FrequencyAlertDto;
import com.example.backend.dto.SystemMetricsDto;
import com.example.backend.service.application.FaultAlertApplicationService;
import com.example.backend.service.application.FrequencyAlertApplicationService;
import com.example.backend.service.application.SystemMetricsApplicationService;
import com.example.backend.service.stream.SseEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    private static final Logger log = LoggerFactory.getLogger(KafkaListeners.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final FaultAlertApplicationService faultAlertApplicationService;
    private final FrequencyAlertApplicationService frequencyAlertApplicationService;
    private final SystemMetricsApplicationService systemMetricsApplicationService;
    private final SseEventService sseEventService;


    public KafkaListeners(FaultAlertApplicationService faultAlertApplicationService,
                          FrequencyAlertApplicationService frequencyAlertApplicationService,
                          SystemMetricsApplicationService systemMetricsApplicationService,
                          SseEventService sseEventService) {
        this.faultAlertApplicationService = faultAlertApplicationService;
        this.frequencyAlertApplicationService = frequencyAlertApplicationService;
        this.systemMetricsApplicationService = systemMetricsApplicationService;
        this.sseEventService = sseEventService;
    }

    @KafkaListener(topics = "${app.kafka.topics.system-metrics}")
    public void onMetric(String json) {
        SystemMetricsDto metrics;
        try {
            metrics = objectMapper.readValue(json, SystemMetricsDto.class);
        } catch (JsonProcessingException exception) {
            log.error("Could not deserialize system metrics: {}", json, exception);
            return;
        }

        systemMetricsApplicationService.save(metrics);
        sseEventService.publish("system-metric", Long.toString(metrics.timestamp()), metrics);
    }

    @KafkaListener(topics = "${app.kafka.topics.fault-alerts}")
    public void onFaultAlert(String json) {
        FaultAlertDto alert;
        try {
            alert = objectMapper.readValue(json, FaultAlertDto.class);
        } catch (JsonProcessingException exception) {
            log.error("Could not deserialize fault alert: {}", json, exception);
            return;
        }

        faultAlertApplicationService.save(alert);
        sseEventService.publish("fault-alert", alert.alertId(), alert);
    }

    @KafkaListener(topics = "${app.kafka.topics.frequency-alerts}")
    public void onFrequencyAlert(String json) {
        FrequencyAlertDto alert;
        try {
            alert = objectMapper.readValue(json, FrequencyAlertDto.class);
        } catch (JsonProcessingException exception) {
            log.error("Could not deserialize frequency alert: {}", json, exception);
            return;
        }

        frequencyAlertApplicationService.save(alert);
        sseEventService.publish("frequency-alert", alert.alertId(), alert);
    }
}
