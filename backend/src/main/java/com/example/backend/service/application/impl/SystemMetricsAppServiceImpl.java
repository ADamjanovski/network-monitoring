package com.example.backend.service.application.impl;

import com.example.backend.dto.SystemMetricsDto;
import com.example.backend.service.application.SystemMetricsApplicationService;
import com.example.backend.service.domain.SystemMetricsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemMetricsAppServiceImpl implements SystemMetricsApplicationService {

    private final SystemMetricsService systemMetricsService;

    public SystemMetricsAppServiceImpl(SystemMetricsService systemMetricsService) {
        this.systemMetricsService = systemMetricsService;
    }

    @Override
    public List<SystemMetricsDto> findAll() {
        return systemMetricsService.findAll().stream().map(SystemMetricsDto::from).collect(Collectors.toList());
    }

    @Override
    public void save(SystemMetricsDto systemMetrics) {
        systemMetricsService.save(systemMetrics.toSystemMetric());
    }

    @Override
    public List<SystemMetricsDto> findAllinTimeframe(Long windowStart, Long windowEnd) {
        return systemMetricsService.findAllinTimeframe(windowStart,windowEnd).stream()
                .map(SystemMetricsDto::from).collect(Collectors.toList());
    }
}
