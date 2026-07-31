package com.example.backend.repository;

import com.example.backend.model.SystemMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics,Long> {

    List<SystemMetrics> findAllByTimestampBetween(Long start, Long end);
}
