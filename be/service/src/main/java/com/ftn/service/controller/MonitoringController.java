package com.ftn.service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.service.dto.monitoring.MonitoringStartRequest;
import com.ftn.service.dto.monitoring.MonitoringStatusDTO;
import com.ftn.service.service.RealTimeSimulationService;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final RealTimeSimulationService service;

    public MonitoringController(RealTimeSimulationService service) {
        this.service = service;
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public MonitoringStatusDTO status() {
        return service.status();
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public MonitoringStatusDTO start(@RequestBody(required = false) MonitoringStartRequest request) {
        boolean cep = request == null || request.isCepEnabled();
        return service.start(cep);
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public MonitoringStatusDTO stop() {
        return service.stop();
    }
}
