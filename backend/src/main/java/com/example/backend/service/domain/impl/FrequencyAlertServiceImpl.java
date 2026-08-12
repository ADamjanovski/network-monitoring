package com.example.backend.service.domain.impl;

import com.example.backend.model.FrequencyAlert;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;
import com.example.backend.repository.FrequencyAlertRepository;
import com.example.backend.service.domain.FrequencyAlertService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FrequencyAlertServiceImpl implements FrequencyAlertService {

    private static final int MAX_RESULTS = 1_000;
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
    public List<FrequencyAlert> findAllInTimeframe(Long windowStart, Long windowEnd) {
        return frequencyAlertRepository.findAllByTimestampBetween(windowStart, windowEnd);
    }

    @Override
    public List<FrequencyAlert> search(Long start, Long end, String region,
                                       FrequencyAlertType alertType, SeverityLevel severityLevel, int limit) {
        return frequencyAlertRepository.search(start, end, region, alertType, severityLevel,
                PageRequest.of(0, normalizeLimit(limit)));
    }

    @Override
    public Optional<FrequencyAlert> findById(String alertId) {
        return frequencyAlertRepository.findById(alertId);
    }

    private int normalizeLimit(int limit) {
        return Math.min(Math.max(limit, 1), MAX_RESULTS);
    }
}
