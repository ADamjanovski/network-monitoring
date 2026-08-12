package com.example.backend.service.application.impl;

import com.example.backend.dto.FaultAlertDto;
import com.example.backend.model.enums.AlertType;
import com.example.backend.model.enums.SeverityLevel;
import com.example.backend.service.application.FaultAlertApplicationService;
import com.example.backend.service.domain.FaultAlertService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FaultAlertAppServiceImpl implements FaultAlertApplicationService {

    private final FaultAlertService faultAlertService;

    public FaultAlertAppServiceImpl(FaultAlertService faultAlertService) {
        this.faultAlertService = faultAlertService;
    }

    @Override
    public List<FaultAlertDto> findAll() {
        return faultAlertService.findAll().stream().map(FaultAlertDto::from).collect(Collectors.toList());
    }

    @Override
    public void save(FaultAlertDto faultAlert) {
        faultAlertService.save(faultAlert.toEntity());
    }

    @Override
    public List<FaultAlertDto> findAllInTimeframe(Long windowStart, Long windowEnd) {
        return faultAlertService.findAllInTimeframe(windowStart, windowEnd).stream()
                .map(FaultAlertDto::from).collect(Collectors.toList());
    }

    @Override
    public List<FaultAlertDto> search(Long start, Long end, String region, String substation, String location, String pmuId,
                                      AlertType alertType, SeverityLevel severityLevel, int limit) {
        return faultAlertService.search(start, end, region, substation, location, pmuId, alertType, severityLevel, limit).stream()
                .map(FaultAlertDto::from).collect(Collectors.toList());
    }

    @Override
    public Optional<FaultAlertDto> findById(String alertId) {
        return faultAlertService.findById(alertId).map(FaultAlertDto::from);
    }
}
