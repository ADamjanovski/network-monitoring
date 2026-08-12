package com.example.backend.repository;

import com.example.backend.model.FaultAlert;
import com.example.backend.model.enums.AlertType;
import com.example.backend.model.enums.SeverityLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaultAlertRepository extends JpaRepository<FaultAlert,String> {
    List<FaultAlert> findAllByTimestampBetween(Long start, Long end);

    @Query("SELECT alert FROM FaultAlert alert " +
            "WHERE (:start IS NULL OR alert.timestamp >= :start) " +
            "AND (:end IS NULL OR alert.timestamp <= :end) " +
            "AND (:region IS NULL OR alert.region = :region) " +
            "AND (:substation IS NULL OR alert.substation = :substation) " +
            "AND (:location IS NULL OR alert.location = :location) " +
            "AND (:pmuId IS NULL OR alert.pmuId = :pmuId) " +
            "AND (:alertType IS NULL OR alert.alertType = :alertType) " +
            "AND (:severityLevel IS NULL OR alert.severityLevel = :severityLevel) " +
            "ORDER BY alert.timestamp DESC")
    List<FaultAlert> search(
            @Param("start") Long start,
            @Param("end") Long end,
            @Param("region") String region,
            @Param("substation") String substation,
            @Param("location") String location,
            @Param("pmuId") String pmuId,
            @Param("alertType") AlertType alertType,
            @Param("severityLevel") SeverityLevel severityLevel,
            Pageable pageable
    );
}
