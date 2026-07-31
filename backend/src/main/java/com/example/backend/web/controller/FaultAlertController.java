package com.example.backend.web.controller;

import com.example.backend.dto.FaultAlertDto;
import com.example.backend.service.application.FaultAlertApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fault-alert")
public class FaultAlertController {

    private final FaultAlertApplicationService faultAlertApplicationService;

    public FaultAlertController(FaultAlertApplicationService faultAlertApplicationService) {
        this.faultAlertApplicationService = faultAlertApplicationService;
    }

//    @Operation(summary = "Get all products", description = "Retrieves a list of all available products.")
    @GetMapping
    public List<FaultAlertDto> findAll() {
        return faultAlertApplicationService.findAll();
    }

    @GetMapping ("/time")
    List<FaultAlertDto> findAllBetweenTimeLine(){
        return  faultAlertApplicationService.findAll();
    }

}
