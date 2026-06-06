package com.ftn.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.ThresholdConfig;
import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.threshold.ThresholdConfigRequest;
import com.ftn.service.dto.threshold.ThresholdConfigResponse;
import com.ftn.service.exception.DuplicateResourceException;
import com.ftn.service.exception.ResourceNotFoundException;
import com.ftn.service.repository.ThresholdConfigRepository;

@Service
@Transactional
public class ThresholdConfigService {

    private final ThresholdConfigRepository repository;

    public ThresholdConfigService(ThresholdConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ThresholdConfigResponse> findAll(Pageable pageable) {
        Page<ThresholdConfig> page = repository.findAll(pageable);
        List<ThresholdConfigResponse> content = page.getContent().stream()
                .map(ThresholdConfigResponse::new).collect(Collectors.toList());
        return new PagedResponse<>(content, page);
    }

    @Transactional(readOnly = true)
    public ThresholdConfigResponse findById(Long id) {
        return new ThresholdConfigResponse(getOrThrow(id));
    }

    public ThresholdConfigResponse create(ThresholdConfigRequest request) {
        if (repository.existsByLocationTypeAndParameterType(request.getLocationType(), request.getParameterType())) {
            throw new DuplicateResourceException("Threshold already exists for "
                    + request.getLocationType() + "/" + request.getParameterType());
        }
        ThresholdConfig t = new ThresholdConfig();
        apply(t, request);
        return new ThresholdConfigResponse(repository.save(t));
    }

    public ThresholdConfigResponse update(Long id, ThresholdConfigRequest request) {
        ThresholdConfig t = getOrThrow(id);
        boolean keyChanged = t.getLocationType() != request.getLocationType()
                || t.getParameterType() != request.getParameterType();
        if (keyChanged && repository.existsByLocationTypeAndParameterType(
                request.getLocationType(), request.getParameterType())) {
            throw new DuplicateResourceException("Threshold already exists for "
                    + request.getLocationType() + "/" + request.getParameterType());
        }
        apply(t, request);
        return new ThresholdConfigResponse(repository.save(t));
    }

    public void delete(Long id) {
        ThresholdConfig t = getOrThrow(id);
        repository.delete(t);
    }

    private void apply(ThresholdConfig t, ThresholdConfigRequest request) {
        t.setLocationType(request.getLocationType());
        t.setParameterType(request.getParameterType());
        t.setNormalMax(request.getNormalMax());
        t.setWarningMax(request.getWarningMax());
        t.setCriticalMax(request.getCriticalMax());
    }

    private ThresholdConfig getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThresholdConfig", id));
    }
}
