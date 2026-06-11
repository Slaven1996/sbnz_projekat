package com.ftn.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.Department;
import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.department.DepartmentRequest;
import com.ftn.service.dto.department.DepartmentResponse;
import com.ftn.service.exception.DuplicateResourceException;
import com.ftn.service.exception.ResourceNotFoundException;
import com.ftn.service.repository.DepartmentRepository;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<DepartmentResponse> findAll(Pageable pageable) {
        Page<Department> page = repository.findAll(pageable);
        List<DepartmentResponse> content = page.getContent().stream()
                .map(DepartmentResponse::new).collect(Collectors.toList());
        return new PagedResponse<>(content, page);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return new DepartmentResponse(getOrThrow(id));
    }

    public DepartmentResponse create(DepartmentRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department code already exists: " + request.getCode());
        }
        Department d = new Department();
        d.setCode(request.getCode());
        d.setName(request.getName());
        d.setDescription(request.getDescription());
        return new DepartmentResponse(repository.save(d));
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department d = getOrThrow(id);
        if (!d.getCode().equals(request.getCode()) && repository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department code already exists: " + request.getCode());
        }
        d.setCode(request.getCode());
        d.setName(request.getName());
        d.setDescription(request.getDescription());
        return new DepartmentResponse(repository.save(d));
    }

    public void delete(Long id) {
        Department d = getOrThrow(id);
        repository.delete(d);
    }

    private Department getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }
}
