package com.ftn.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ftn.model.TagUnit;

@Repository
public interface TagUnitRepository extends JpaRepository<TagUnit, Long> {

    Optional<TagUnit> findByCode(String code);

    boolean existsByCode(String code);
}
