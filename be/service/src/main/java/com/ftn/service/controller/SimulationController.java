package com.ftn.service.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.service.dto.simulation.SimulationResultDTO;
import com.ftn.service.service.HistoricalSimulationService;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final HistoricalSimulationService service;

    public SimulationController(HistoricalSimulationService service) {
        this.service = service;
    }

    @GetMapping
    public SimulationResultDTO simulate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false, defaultValue = "HOUR") String step) {
        return service.simulate(startDate, endDate, locationCode, step);
    }
}
