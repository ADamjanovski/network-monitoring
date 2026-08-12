package com.example.backend.service.application;

import com.example.backend.dto.SystemMetricsDto;

import java.util.List;
import java.util.Optional;

public interface SystemMetricsApplicationService {
    List<SystemMetricsDto> findAll();

    void save(SystemMetricsDto systemMetrics);

    List<SystemMetricsDto> findAllInTimeframe(Long windowStart, Long windowEnd);

    Optional<SystemMetricsDto> findLatest();

    List<SystemMetricsDto> search(Long start, Long end, int limit);

    Optional<SystemMetricsDto> findByTimestamp(Long timestamp);
}
