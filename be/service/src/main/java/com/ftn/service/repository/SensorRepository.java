package com.ftn.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ftn.model.Sensor;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByTagName(String tagName);

    boolean existsByTagName(String tagName);

    long countByLocationId(Long locationId);

    @Query("SELECT s.location.id, COUNT(s) FROM Sensor s GROUP BY s.location.id")
    List<Object[]> countGroupedByLocation();
}
