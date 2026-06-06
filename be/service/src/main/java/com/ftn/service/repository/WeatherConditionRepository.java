package com.ftn.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ftn.model.WeatherCondition;

@Repository
public interface WeatherConditionRepository extends JpaRepository<WeatherCondition, Long> {

    Optional<WeatherCondition> findByLocationId(Long locationId);
}
