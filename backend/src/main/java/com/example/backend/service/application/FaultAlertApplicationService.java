package com.example.backend.service.application;

import com.example.backend.dto.FaultAlertDto;
import com.example.backend.model.enums.AlertType;
import com.example.backend.model.enums.SeverityLevel;

import java.util.List;
import java.util.Optional;

public interface FaultAlertApplicationService {

    List<FaultAlertDto> findAll();

    void save(FaultAlertDto faultAlert);

    List<FaultAlertDto> findAllInTimeframe(Long windowStart, Long windowEnd);

    List<FaultAlertDto> search(Long start, Long end, String region, String substation, String location, String pmuId,
                               AlertType alertType, SeverityLevel severityLevel, int limit);

    Optional<FaultAlertDto> findById(String alertId);
}
