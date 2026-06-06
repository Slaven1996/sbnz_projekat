package com.ftn.service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.TrendData;
import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.trenddata.TrendDataResponse;
import com.ftn.service.exception.BadRequestException;
import com.ftn.service.exception.ResourceNotFoundException;
import com.ftn.service.repository.TrendDataRepository;

@Service
@Transactional(readOnly = true)
public class TrendDataService {

    private final TrendDataRepository repository;

    public TrendDataService(TrendDataRepository repository) {
        this.repository = repository;
    }

    public PagedResponse<TrendDataResponse> search(String locationCode, String tagName,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }
        Page<TrendData> page = repository.search(
                emptyToNull(locationCode), emptyToNull(tagName), startDate, endDate, pageable);
        List<TrendDataResponse> content = page.getContent().stream()
                .map(TrendDataResponse::new).collect(Collectors.toList());
        return new PagedResponse<>(content, page);
    }

    public TrendDataResponse findById(Long id) {
        TrendData t = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrendData", id));
        return new TrendDataResponse(t);
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }
}
