package com.example.backend.service.domain.impl;

import com.example.backend.model.FaultAlert;
import com.example.backend.repository.FaultAlertRepository;
import com.example.backend.service.domain.FaultAlertService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaultAlertServiceImpl implements FaultAlertService {

    final private FaultAlertRepository faultAlertRepository;

    public FaultAlertServiceImpl(FaultAlertRepository faultAlertRepository) {
        this.faultAlertRepository = faultAlertRepository;
    }

    @Override
    public List<FaultAlert> findAll() {
        return faultAlertRepository.findAll();
    }

    @Override
    public void save(FaultAlert faultAlert) {
        faultAlertRepository.save(faultAlert);
    }

    @Override
    public List<FaultAlert> findAllinTimeframe(Long windowStart, Long windowEnd) {
        return faultAlertRepository.findAllByTimestampBetween(windowStart,windowEnd);
    }
}
