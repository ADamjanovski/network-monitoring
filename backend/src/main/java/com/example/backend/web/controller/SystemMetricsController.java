package com.example.backend.web.controller;

import com.example.backend.dto.SystemMetricsDto;
import com.example.backend.service.application.SystemMetricsApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SystemMetricsController {

    private final SystemMetricsApplicationService systemMetricsApplicationService;

    public SystemMetricsController(SystemMetricsApplicationService systemMetricsApplicationService) {
        this.systemMetricsApplicationService = systemMetricsApplicationService;
    }

    @GetMapping
    public List<SystemMetricsDto> findAll(){
        return systemMetricsApplicationService.findAll();
    }
}
