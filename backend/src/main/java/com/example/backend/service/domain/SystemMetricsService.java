package com.example.backend.service.domain;

import com.example.backend.model.SystemMetrics;

import java.util.List;

public interface SystemMetricsService {
    List<SystemMetrics> findAll();

    void save(SystemMetrics systemMetrics);

    List<SystemMetrics> findAllinTimeframe(Long windowStart,Long windowEnd);
}
