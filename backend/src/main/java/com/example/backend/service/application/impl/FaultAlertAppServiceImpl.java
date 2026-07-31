package com.example.backend.service.application.impl;

import com.example.backend.dto.FaultAlertDto;
import com.example.backend.service.application.FaultAlertApplicationService;
import com.example.backend.service.domain.FaultAlertService;
import org.springframework.stereotype.Service;

import java.util.List;
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
        faultAlertService.save(faultAlert.toFaultAlert());
    }

    @Override
    public List<FaultAlertDto> findAllinTimeframe(Long windowStart, Long windowEnd) {
        return faultAlertService.findAllinTimeframe(windowStart,windowEnd).stream()
                .map(FaultAlertDto::from).collect(Collectors.toList());
    }
}
