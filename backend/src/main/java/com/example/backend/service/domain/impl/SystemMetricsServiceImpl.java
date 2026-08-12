package com.example.backend.service.domain.impl;

import com.example.backend.model.SystemMetrics;
import com.example.backend.repository.SystemMetricsRepository;
import com.example.backend.service.domain.SystemMetricsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class SystemMetricsServiceImpl implements SystemMetricsService {

    private static final int MAX_RESULTS = 5_000;
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
    public List<SystemMetrics> findAllInTimeframe(Long windowStart, Long windowEnd) {
        return systemMetricsRepository.findAllByTimestampBetween(windowStart, windowEnd);
    }

    @Override
    public Optional<SystemMetrics> findLatest() {
        return systemMetricsRepository.findFirstByOrderByTimestampDesc();
    }

    @Override
    public List<SystemMetrics> search(Long start, Long end, int limit) {
        int normalizedLimit = normalizeLimit(limit);
        if (start != null && end != null) {
            List<SystemMetrics> metrics = systemMetricsRepository
                    .findAllByTimestampBetweenOrderByTimestampAsc(start, end);
            return evenlySampleNewestFirst(metrics, normalizedLimit);
        }
        return systemMetricsRepository.search(start, end, PageRequest.of(0, normalizedLimit));
    }

    @Override
    public Optional<SystemMetrics> findByTimestamp(Long timestamp) {
        return systemMetricsRepository.findById(timestamp);
    }

    private int normalizeLimit(int limit) {
        return Math.min(Math.max(limit, 1), MAX_RESULTS);
    }

    private List<SystemMetrics> evenlySampleNewestFirst(List<SystemMetrics> metrics, int limit) {
        if (metrics.size() <= limit) {
            return IntStream.range(0, metrics.size())
                    .map(index -> metrics.size() - 1 - index)
                    .mapToObj(metrics::get)
                    .toList();
        }
        if (limit == 1) {
            return List.of(metrics.get(metrics.size() - 1));
        }

        double step = (double) (metrics.size() - 1) / (limit - 1);
        return IntStream.range(0, limit)
                .map(index -> limit - 1 - index)
                .mapToObj(index -> metrics.get((int) Math.round(index * step)))
                .toList();
    }
}
