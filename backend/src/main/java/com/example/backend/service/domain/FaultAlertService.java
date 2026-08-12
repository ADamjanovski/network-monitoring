package com.example.backend.service.domain;

import com.example.backend.model.FaultAlert;
import com.example.backend.model.enums.AlertType;
import com.example.backend.model.enums.SeverityLevel;

import java.util.List;
import java.util.Optional;

public interface FaultAlertService {
    List<FaultAlert> findAll();

    void save(FaultAlert faultAlert);

    List<FaultAlert> findAllInTimeframe(Long windowStart, Long windowEnd);

    List<FaultAlert> search(Long start, Long end, String region, String substation, String location, String pmuId,
                            AlertType alertType, SeverityLevel severityLevel, int limit);

    Optional<FaultAlert> findById(String alertId);
}
