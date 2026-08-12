package com.example.backend.service.domain;

import com.example.backend.model.SystemMetrics;

import java.util.List;
import java.util.Optional;

public interface SystemMetricsService {
    List<SystemMetrics> findAll();

    void save(SystemMetrics systemMetrics);

    List<SystemMetrics> findAllInTimeframe(Long windowStart, Long windowEnd);

    Optional<SystemMetrics> findLatest();

    List<SystemMetrics> search(Long start, Long end, int limit);

    Optional<SystemMetrics> findByTimestamp(Long timestamp);
}
