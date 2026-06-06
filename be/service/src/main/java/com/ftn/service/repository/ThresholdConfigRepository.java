package com.ftn.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ftn.model.ThresholdConfig;
import com.ftn.model.enums.LocationType;
import com.ftn.model.enums.ParameterType;

@Repository
public interface ThresholdConfigRepository extends JpaRepository<ThresholdConfig, Long> {

    Optional<ThresholdConfig> findByLocationTypeAndParameterType(LocationType locationType, ParameterType parameterType);

    boolean existsByLocationTypeAndParameterType(LocationType locationType, ParameterType parameterType);
}
