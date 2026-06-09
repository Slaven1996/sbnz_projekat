package com.ftn.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.Location;
import com.ftn.model.Sensor;
import com.ftn.model.TagUnit;
import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.sensor.SensorRequest;
import com.ftn.service.dto.sensor.SensorResponse;
import com.ftn.service.exception.DuplicateResourceException;
import com.ftn.service.exception.ResourceNotFoundException;
import com.ftn.service.repository.LocationRepository;
import com.ftn.service.repository.SensorRepository;
import com.ftn.service.repository.TagUnitRepository;

@Service
@Transactional
public class SensorService {

    private final SensorRepository sensorRepository;
    private final LocationRepository locationRepository;
    private final TagUnitRepository tagUnitRepository;

    public SensorService(SensorRepository sensorRepository, LocationRepository locationRepository,
                         TagUnitRepository tagUnitRepository) {
        this.sensorRepository = sensorRepository;
        this.locationRepository = locationRepository;
        this.tagUnitRepository = tagUnitRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<SensorResponse> findAll(Pageable pageable) {
        Page<Sensor> page = sensorRepository.findAll(pageable);
        List<SensorResponse> content = page.getContent().stream()
                .map(SensorResponse::new).collect(Collectors.toList());
        return new PagedResponse<>(content, page);
    }

    @Transactional(readOnly = true)
    public SensorResponse findById(Long id) {
        return new SensorResponse(getOrThrow(id));
    }

    public SensorResponse create(SensorRequest request) {
        if (sensorRepository.existsByTagName(request.getTagName())) {
            throw new DuplicateResourceException("Sensor tagName already exists: " + request.getTagName());
        }
        Sensor s = new Sensor();
        applyFields(s, request);
        return new SensorResponse(sensorRepository.save(s));
    }

    public SensorResponse update(Long id, SensorRequest request) {
        Sensor s = getOrThrow(id);
        if (!s.getTagName().equals(request.getTagName()) && sensorRepository.existsByTagName(request.getTagName())) {
            throw new DuplicateResourceException("Sensor tagName already exists: " + request.getTagName());
        }
        applyFields(s, request);
        return new SensorResponse(sensorRepository.save(s));
    }

    public void delete(Long id) {
        Sensor s = getOrThrow(id);
        sensorRepository.delete(s);
    }

    private void applyFields(Sensor s, SensorRequest request) {
        s.setLocation(resolveLocation(request.getLocationId()));
        s.setUnit(resolveUnit(request.getUnitId()));
        s.setTagName(request.getTagName());
        s.setDisplayCode(request.getDisplayCode());
        s.setSensorType(request.getSensorType());
        s.setEngLow(request.getEngLow());
        s.setEngHigh(request.getEngHigh());
        s.setRawLow(request.getRawLow());
        s.setRawHigh(request.getRawHigh());
    }

    private Location resolveLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));
    }

    private TagUnit resolveUnit(Long unitId) {
        if (unitId == null) {
            return null;
        }
        return tagUnitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("TagUnit", unitId));
    }

    private Sensor getOrThrow(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }
}
