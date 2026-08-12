package com.example.backend.service.application.impl;

import com.example.backend.dto.SystemMetricsDto;
import com.example.backend.service.application.SystemMetricsApplicationService;
import com.example.backend.service.domain.SystemMetricsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
        systemMetricsService.save(systemMetrics.toEntity());
    }

    @Override
    public List<SystemMetricsDto> findAllInTimeframe(Long windowStart, Long windowEnd) {
        return systemMetricsService.findAllInTimeframe(windowStart, windowEnd).stream()
                .map(SystemMetricsDto::from).collect(Collectors.toList());
    }

    @Override
    public Optional<SystemMetricsDto> findLatest() {
        return systemMetricsService.findLatest().map(SystemMetricsDto::from);
    }

    @Override
    public List<SystemMetricsDto> search(Long start, Long end, int limit) {
        return systemMetricsService.search(start, end, limit).stream()
                .map(SystemMetricsDto::from).collect(Collectors.toList());
    }

    @Override
    public Optional<SystemMetricsDto> findByTimestamp(Long timestamp) {
        return systemMetricsService.findByTimestamp(timestamp).map(SystemMetricsDto::from);
    }
}
