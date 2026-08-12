package com.example.backend.service.domain;

import com.example.backend.model.FrequencyAlert;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;

import java.util.List;
import java.util.Optional;

public interface FrequencyAlertService {
    List<FrequencyAlert> findAll();

    void save(FrequencyAlert frequencyAlert);

    List<FrequencyAlert> findAllInTimeframe(Long windowStart, Long windowEnd);

    List<FrequencyAlert> search(Long start, Long end, String region,
                                FrequencyAlertType alertType, SeverityLevel severityLevel, int limit);

    Optional<FrequencyAlert> findById(String alertId);
}
