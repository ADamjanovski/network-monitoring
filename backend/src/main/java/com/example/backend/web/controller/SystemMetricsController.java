package com.example.backend.web.controller;

import com.example.backend.dto.SystemMetricsDto;
import com.example.backend.service.application.SystemMetricsApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system-metrics")
public class SystemMetricsController {

    private final SystemMetricsApplicationService systemMetricsApplicationService;

    public SystemMetricsController(SystemMetricsApplicationService systemMetricsApplicationService) {
        this.systemMetricsApplicationService = systemMetricsApplicationService;
    }

    @GetMapping
    public List<SystemMetricsDto> search(
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @RequestParam(defaultValue = "2000") int limit
    ) {
        return systemMetricsApplicationService.search(start, end, limit);
    }

    @GetMapping("/latest")
    public ResponseEntity<SystemMetricsDto> findLatest() {
        return systemMetricsApplicationService.findLatest()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{timestamp}")
    public ResponseEntity<SystemMetricsDto> findByTimestamp(@PathVariable Long timestamp) {
        return ResponseEntity.of(systemMetricsApplicationService.findByTimestamp(timestamp));
    }
}
