package com.example.backend.service.domain;

import com.example.backend.model.FrequencyAlert;

import java.util.List;
import java.util.Optional;

public interface FrequencyAlertService {
    List<FrequencyAlert> findAll();

    void save(FrequencyAlert frequencyAlert);

    List<FrequencyAlert> findAllinTimeframe(Long windowStart,Long windowEnd);
}
