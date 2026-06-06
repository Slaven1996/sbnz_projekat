package com.ftn.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.TagUnit;
import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.tagunit.TagUnitRequest;
import com.ftn.service.dto.tagunit.TagUnitResponse;
import com.ftn.service.exception.DuplicateResourceException;
import com.ftn.service.exception.ResourceNotFoundException;
import com.ftn.service.repository.TagUnitRepository;

@Service
@Transactional
public class TagUnitService {

    private final TagUnitRepository repository;

    public TagUnitService(TagUnitRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<TagUnitResponse> findAll(Pageable pageable) {
        Page<TagUnit> page = repository.findAll(pageable);
        List<TagUnitResponse> content = page.getContent().stream()
                .map(TagUnitResponse::new).collect(Collectors.toList());
        return new PagedResponse<>(content, page);
    }

    @Transactional(readOnly = true)
    public TagUnitResponse findById(Long id) {
        return new TagUnitResponse(getOrThrow(id));
    }

    public TagUnitResponse create(TagUnitRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Unit code already exists: " + request.getCode());
        }
        TagUnit t = new TagUnit();
        t.setCode(request.getCode());
        t.setUnit(request.getUnit());
        t.setDescription(request.getDescription());
        return new TagUnitResponse(repository.save(t));
    }

    public TagUnitResponse update(Long id, TagUnitRequest request) {
        TagUnit t = getOrThrow(id);
        if (!t.getCode().equals(request.getCode()) && repository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Unit code already exists: " + request.getCode());
        }
        t.setCode(request.getCode());
        t.setUnit(request.getUnit());
        t.setDescription(request.getDescription());
        return new TagUnitResponse(repository.save(t));
    }

    public void delete(Long id) {
        TagUnit t = getOrThrow(id);
        repository.delete(t);
    }

    private TagUnit getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TagUnit", id));
    }
}
