package com.example.backend.service.application;

import com.example.backend.dto.FrequencyAlertDto;

import java.util.List;

public interface FrequencyAlertApplicationService {
    List<FrequencyAlertDto> findAll();

    void save(FrequencyAlertDto frequencyAlert);

    List<FrequencyAlertDto> findAllinTimeframe(Long windowStart,Long windowEnd);
}
