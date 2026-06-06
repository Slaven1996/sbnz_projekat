package com.ftn.service.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.trenddata.TrendDataResponse;
import com.ftn.service.service.TrendDataService;

@RestController
@RequestMapping("/api/trend-data")
public class TrendDataController {

    private final TrendDataService service;

    public TrendDataController(TrendDataService service) {
        this.service = service;
    }

    @GetMapping
    public PagedResponse<TrendDataResponse> search(
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 50, sort = "logTime") Pageable pageable) {
        return service.search(locationCode, tagName, startDate, endDate, pageable);
    }

    @GetMapping("/{id}")
    public TrendDataResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }
}
