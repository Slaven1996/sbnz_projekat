package com.ftn.service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.Location;
import com.ftn.model.WeatherCondition;
import com.ftn.model.Zone;
import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.location.LocationRequest;
import com.ftn.service.dto.location.LocationResponse;
import com.ftn.service.dto.location.WeatherConditionDto;
import com.ftn.service.exception.DuplicateResourceException;
import com.ftn.service.exception.ResourceNotFoundException;
import com.ftn.service.repository.LocationRepository;
import com.ftn.service.repository.WeatherConditionRepository;
import com.ftn.service.repository.ZoneRepository;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final WeatherConditionRepository weatherRepository;

    public LocationService(LocationRepository locationRepository,
                           ZoneRepository zoneRepository, WeatherConditionRepository weatherRepository) {
        this.locationRepository = locationRepository;
        this.zoneRepository = zoneRepository;
        this.weatherRepository = weatherRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<LocationResponse> findAll(Pageable pageable) {
        Page<Location> page = locationRepository.findAll(pageable);
        List<LocationResponse> content = page.getContent().stream()
                .map(LocationResponse::new).collect(Collectors.toList());
        return new PagedResponse<>(content, page);
    }

    @Transactional(readOnly = true)
    public LocationResponse findById(Long id) {
        return new LocationResponse(getOrThrow(id));
    }

    public LocationResponse create(LocationRequest request) {
        if (locationRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Location code already exists: " + request.getCode());
        }
        Location l = new Location();
        applyScalarFields(l, request);
        l.setZone(resolveZone(request.getZoneId()));
        Location saved = locationRepository.save(l);

        upsertWeather(saved, request.getWeatherCondition());
        return new LocationResponse(saved);
    }

    public LocationResponse update(Long id, LocationRequest request) {
        Location l = getOrThrow(id);
        if (!l.getCode().equals(request.getCode()) && locationRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Location code already exists: " + request.getCode());
        }
        applyScalarFields(l, request);
        l.setZone(resolveZone(request.getZoneId()));
        Location saved = locationRepository.save(l);

        upsertWeather(saved, request.getWeatherCondition());
        return new LocationResponse(saved);
    }

    public void delete(Long id) {
        Location l = getOrThrow(id);
        weatherRepository.findByLocationId(id).ifPresent(weatherRepository::delete);
        locationRepository.delete(l);
    }

    private void applyScalarFields(Location l, LocationRequest request) {
        l.setCode(request.getCode());
        l.setDisplayCode(request.getDisplayCode() != null ? request.getDisplayCode() : request.getCode());
        l.setType(request.getType());
        l.setPosX(request.getPosX());
        l.setPosY(request.getPosY());
        l.setActive(request.getActive() == null || request.getActive());
    }

    private void upsertWeather(Location location, WeatherConditionDto dto) {
        WeatherCondition existing = weatherRepository.findByLocationId(location.getId()).orElse(null);
        if (dto == null) {
            if (existing != null) {
                weatherRepository.delete(existing);
                location.setWeatherCondition(null);
            }
            return;
        }
        WeatherCondition weather = existing != null ? existing : new WeatherCondition();
        weather.setLocation(location);
        weather.setPrecipitation(dto.getPrecipitation());
        weather.setLastUpdate(LocalDateTime.now());
        location.setWeatherCondition(weatherRepository.save(weather));
    }

    private Zone resolveZone(Long zoneId) {
        if (zoneId == null) {
            return null;
        }
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", zoneId));
    }

    private Location getOrThrow(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
    }
}
