package com.example.backend.repository;

import com.example.backend.model.SystemMetrics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics,Long> {

    List<SystemMetrics> findAllByTimestampBetween(Long start, Long end);

    List<SystemMetrics> findAllByTimestampBetweenOrderByTimestampAsc(Long start, Long end);

    Optional<SystemMetrics> findFirstByOrderByTimestampDesc();

    @Query("SELECT metrics FROM SystemMetrics metrics " +
            "WHERE (:start IS NULL OR metrics.timestamp >= :start) " +
            "AND (:end IS NULL OR metrics.timestamp <= :end) " +
            "ORDER BY metrics.timestamp DESC")
    List<SystemMetrics> search(
            @Param("start") Long start,
            @Param("end") Long end,
            Pageable pageable
    );
}
