package com.example.backend.service.domain;

import com.example.backend.model.FaultAlert;

import java.util.List;

public interface FaultAlertService {
    List<FaultAlert> findAll();

    void save(FaultAlert faultAlert);

    List<FaultAlert> findAllinTimeframe(Long windowStart,Long windowEnd);
}
