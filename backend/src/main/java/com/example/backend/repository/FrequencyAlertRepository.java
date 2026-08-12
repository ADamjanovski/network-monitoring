package com.example.backend.repository;

import com.example.backend.model.FrequencyAlert;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequencyAlertRepository extends JpaRepository<FrequencyAlert, String> {

    List<FrequencyAlert> findAllByTimestampBetween(Long start, Long end);

    @Query("SELECT alert FROM FrequencyAlert alert " +
            "WHERE (:start IS NULL OR alert.timestamp >= :start) " +
            "AND (:end IS NULL OR alert.timestamp <= :end) " +
            "AND (:region IS NULL OR alert.region = :region) " +
            "AND (:alertType IS NULL OR alert.alertType = :alertType) " +
            "AND (:severityLevel IS NULL OR alert.severityLevel = :severityLevel) " +
            "ORDER BY alert.timestamp DESC")
    List<FrequencyAlert> search(
            @Param("start") Long start,
            @Param("end") Long end,
            @Param("region") String region,
            @Param("alertType") FrequencyAlertType alertType,
            @Param("severityLevel") SeverityLevel severityLevel,
            Pageable pageable
    );
}
