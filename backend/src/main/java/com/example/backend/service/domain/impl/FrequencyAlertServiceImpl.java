package com.example.backend.service.domain.impl;

import com.example.backend.model.FrequencyAlert;
import com.example.backend.repository.FrequencyAlertRepository;
import com.example.backend.service.domain.FrequencyAlertService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FrequencyAlertServiceImpl implements FrequencyAlertService {

    private final FrequencyAlertRepository frequencyAlertRepository;

    public FrequencyAlertServiceImpl(FrequencyAlertRepository frequencyAlertRepository) {
        this.frequencyAlertRepository = frequencyAlertRepository;
    }

    @Override
    public List<FrequencyAlert> findAll() {
        return frequencyAlertRepository.findAll();
    }

    @Override
    public void save(FrequencyAlert frequencyAlert) {
        frequencyAlertRepository.save(frequencyAlert);
    }

    @Override
    public List<FrequencyAlert> findAllinTimeframe(Long windowStart, Long windowEnd) {
        return frequencyAlertRepository.findAllByTimestampBetween(windowStart,windowEnd);
    }
}
