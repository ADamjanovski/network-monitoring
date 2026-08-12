package com.example.backend.web.controller;

import com.example.backend.dto.FaultAlertDto;
import com.example.backend.model.enums.AlertType;
import com.example.backend.model.enums.SeverityLevel;
import com.example.backend.service.application.FaultAlertApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fault-alert")
public class FaultAlertController {

    private final FaultAlertApplicationService faultAlertApplicationService;

    public FaultAlertController(FaultAlertApplicationService faultAlertApplicationService) {
        this.faultAlertApplicationService = faultAlertApplicationService;
    }

    @GetMapping
    public List<FaultAlertDto> search(
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String substation,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String pmuId,
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) SeverityLevel severityLevel,
            @RequestParam(defaultValue = "200") int limit
    ) {
        return faultAlertApplicationService.search(
                start, end, region, substation, location, pmuId, alertType, severityLevel, limit
        );
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<FaultAlertDto> findById(@PathVariable String alertId) {
        return ResponseEntity.of(faultAlertApplicationService.findById(alertId));
    }
}
