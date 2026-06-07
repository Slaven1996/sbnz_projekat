package com.ftn.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.Zone;
import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.zone.ZoneRequest;
import com.ftn.service.dto.zone.ZoneResponse;
import com.ftn.service.exception.DuplicateResourceException;
import com.ftn.service.exception.ResourceNotFoundException;
import com.ftn.service.repository.ZoneRepository;

@Service
@Transactional
public class ZoneService {

    private final ZoneRepository repository;

    public ZoneService(ZoneRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ZoneResponse> findAll(Pageable pageable) {
        Page<Zone> page = repository.findAll(pageable);
        List<ZoneResponse> content = page.getContent().stream()
                .map(ZoneResponse::new).collect(Collectors.toList());
        return new PagedResponse<>(content, page);
    }

    @Transactional(readOnly = true)
    public ZoneResponse findById(Long id) {
        return new ZoneResponse(getOrThrow(id));
    }

    public ZoneResponse create(ZoneRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Zone code already exists: " + request.getCode());
        }
        Zone z = new Zone();
        z.setCode(request.getCode());
        z.setName(request.getName());
        z.setDescription(request.getDescription());
        return new ZoneResponse(repository.save(z));
    }

    public ZoneResponse update(Long id, ZoneRequest request) {
        Zone z = getOrThrow(id);
        if (!z.getCode().equals(request.getCode()) && repository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Zone code already exists: " + request.getCode());
        }
        z.setCode(request.getCode());
        z.setName(request.getName());
        z.setDescription(request.getDescription());
        return new ZoneResponse(repository.save(z));
    }

    public void delete(Long id) {
        Zone z = getOrThrow(id);
        repository.delete(z);
    }

    private Zone getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", id));
    }
}
