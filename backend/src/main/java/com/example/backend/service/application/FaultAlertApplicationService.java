package com.example.backend.service.application;

import com.example.backend.dto.FaultAlertDto;

import java.util.List;

public interface FaultAlertApplicationService {

    List<FaultAlertDto> findAll();

    void save(FaultAlertDto faultAlert);

    List<FaultAlertDto> findAllinTimeframe(Long windowStart,Long windowEnd);
}
