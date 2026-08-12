package com.example.backend.service.domain.impl;

import com.example.backend.model.FaultAlert;
import com.example.backend.model.enums.AlertType;
import com.example.backend.model.enums.SeverityLevel;
import com.example.backend.repository.FaultAlertRepository;
import com.example.backend.service.domain.FaultAlertService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FaultAlertServiceImpl implements FaultAlertService {

    private static final int MAX_RESULTS = 1_000;
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
    public List<FaultAlert> findAllInTimeframe(Long windowStart, Long windowEnd) {
        return faultAlertRepository.findAllByTimestampBetween(windowStart, windowEnd);
    }

    @Override
    public List<FaultAlert> search(Long start, Long end, String region, String substation, String location, String pmuId,
                                   AlertType alertType, SeverityLevel severityLevel, int limit) {
        return faultAlertRepository.search(start, end, region, substation, location, pmuId, alertType, severityLevel,
                PageRequest.of(0, normalizeLimit(limit)));
    }

    @Override
    public Optional<FaultAlert> findById(String alertId) {
        return faultAlertRepository.findById(alertId);
    }

    private int normalizeLimit(int limit) {
        return Math.min(Math.max(limit, 1), MAX_RESULTS);
    }
}
