package com.example.backend.service.application;

import com.example.backend.dto.FrequencyAlertDto;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;

import java.util.List;
import java.util.Optional;

public interface FrequencyAlertApplicationService {
    List<FrequencyAlertDto> findAll();

    void save(FrequencyAlertDto frequencyAlert);

    List<FrequencyAlertDto> findAllInTimeframe(Long windowStart, Long windowEnd);

    List<FrequencyAlertDto> search(Long start, Long end, String region,
                                   FrequencyAlertType alertType, SeverityLevel severityLevel, int limit);

    Optional<FrequencyAlertDto> findById(String alertId);
}
