package com.example.backend.web.controller;

import com.example.backend.dto.FrequencyAlertDto;
import com.example.backend.service.application.FrequencyAlertApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<FrequencyAlertDto> findAll(){
        return frequencyAlertApplicationService.findAll();
    }

    @GetMapping("/time")
    public List<FrequencyAlertDto> findAllBetween(){
        return frequencyAlertApplicationService.findAll();
    }
}
