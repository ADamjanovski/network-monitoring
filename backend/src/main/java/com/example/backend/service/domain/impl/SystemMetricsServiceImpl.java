package com.example.backend.service.domain.impl;

import com.example.backend.model.SystemMetrics;
import com.example.backend.repository.SystemMetricsRepository;
import com.example.backend.service.domain.SystemMetricsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemMetricsServiceImpl implements SystemMetricsService {

    private final SystemMetricsRepository systemMetricsRepository;

    public SystemMetricsServiceImpl(SystemMetricsRepository systemMetricsRepository) {
        this.systemMetricsRepository = systemMetricsRepository;
    }

    @Override
    public List<SystemMetrics> findAll() {
        return systemMetricsRepository.findAll();
    }

    @Override
    public void save(SystemMetrics systemMetrics) {
        systemMetricsRepository.save(systemMetrics);
    }

    @Override
    public List<SystemMetrics> findAllinTimeframe(Long windowStart, Long windowEnd) {
        return systemMetricsRepository.findAllByTimestampBetween(windowStart,windowEnd);
    }
}
