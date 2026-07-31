package com.example.backend.service.application.impl;

import com.example.backend.dto.FrequencyAlertDto;
import com.example.backend.service.application.FrequencyAlertApplicationService;
import com.example.backend.service.domain.FrequencyAlertService;
import org.springframework.stereotype.Service;

import java.util.List;
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
        frequencyAlertService.save(frequencyAlert.to());
    }

    @Override
    public List<FrequencyAlertDto> findAllinTimeframe(Long windowStart, Long windowEnd) {
        return frequencyAlertService.findAllinTimeframe(windowStart,windowEnd).stream()
                .map(FrequencyAlertDto::from).collect(Collectors.toList());
    }
}
