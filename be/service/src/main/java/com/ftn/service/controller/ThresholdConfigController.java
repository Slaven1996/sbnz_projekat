package com.ftn.service.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.threshold.ThresholdConfigRequest;
import com.ftn.service.dto.threshold.ThresholdConfigResponse;
import com.ftn.service.service.ThresholdConfigService;

@RestController
@RequestMapping("/api/threshold-configs")
public class ThresholdConfigController {

    private final ThresholdConfigService service;

    public ThresholdConfigController(ThresholdConfigService service) {
        this.service = service;
    }

    @GetMapping
    public PagedResponse<ThresholdConfigResponse> getAll(
            @PageableDefault(size = 20, sort = "locationType") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ThresholdConfigResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<ThresholdConfigResponse> create(@Valid @RequestBody ThresholdConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ThresholdConfigResponse update(@PathVariable Long id, @Valid @RequestBody ThresholdConfigRequest request) {
        return service.update(id, request);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
