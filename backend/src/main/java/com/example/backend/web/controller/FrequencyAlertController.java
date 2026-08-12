package com.example.backend.web.controller;

import com.example.backend.dto.FrequencyAlertDto;
import com.example.backend.model.enums.FrequencyAlertType;
import com.example.backend.model.enums.SeverityLevel;
import com.example.backend.service.application.FrequencyAlertApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/frequency-alert")
public class FrequencyAlertController {

    private final FrequencyAlertApplicationService frequencyAlertApplicationService;

    public FrequencyAlertController(FrequencyAlertApplicationService frequencyAlertApplicationService) {
        this.frequencyAlertApplicationService = frequencyAlertApplicationService;
    }

    @GetMapping
    public List<FrequencyAlertDto> search(
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) FrequencyAlertType alertType,
            @RequestParam(required = false) SeverityLevel severityLevel,
            @RequestParam(defaultValue = "200") int limit
    ) {
        return frequencyAlertApplicationService.search(
                start, end, region, alertType, severityLevel, limit
        );
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<FrequencyAlertDto> findById(@PathVariable String alertId) {
        return ResponseEntity.of(frequencyAlertApplicationService.findById(alertId));
    }
}
