package com.example.backend.service.application.impl;

import com.example.backend.dto.FrequencyAlertDto;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;
import com.example.backend.service.application.FrequencyAlertApplicationService;
import com.example.backend.service.domain.FrequencyAlertService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FrequencyAlertAppServiceImpl implements FrequencyAlertApplicationService {

    private final FrequencyAlertService frequencyAlertService;

    public FrequencyAlertAppServiceImpl(FrequencyAlertService frequencyAlertService) {
        this.frequencyAlertService = frequencyAlertService;
    }

    @Override
    public List<FrequencyAlertDto> findAll() {
        return frequencyAlertService.findAll().stream().map(FrequencyAlertDto::from).collect(Collectors.toList());
    }

    @Override
    public void save(FrequencyAlertDto frequencyAlert) {
        frequencyAlertService.save(frequencyAlert.toEntity());
    }

    @Override
    public List<FrequencyAlertDto> findAllInTimeframe(Long windowStart, Long windowEnd) {
        return frequencyAlertService.findAllInTimeframe(windowStart, windowEnd).stream()
                .map(FrequencyAlertDto::from).collect(Collectors.toList());
    }

    @Override
    public List<FrequencyAlertDto> search(Long start, Long end, String region,
                                          FrequencyAlertType alertType, SeverityLevel severityLevel, int limit) {
        return frequencyAlertService.search(start, end, region, alertType, severityLevel, limit).stream()
                .map(FrequencyAlertDto::from).collect(Collectors.toList());
    }

    @Override
    public Optional<FrequencyAlertDto> findById(String alertId) {
        return frequencyAlertService.findById(alertId).map(FrequencyAlertDto::from);
    }
}
