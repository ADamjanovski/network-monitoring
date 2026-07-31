package com.example.backend.service.application;

import com.example.backend.dto.SystemMetricsDto;

import java.util.List;

public interface SystemMetricsApplicationService {
    List<SystemMetricsDto> findAll();

    void save(SystemMetricsDto systemMetrics);

    List<SystemMetricsDto> findAllinTimeframe(Long windowStart,Long windowEnd);
}
