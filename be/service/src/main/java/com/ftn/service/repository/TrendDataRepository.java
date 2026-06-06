package com.ftn.service.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ftn.model.TrendData;

@Repository
public interface TrendDataRepository extends JpaRepository<TrendData, Long> {

    @Query("SELECT t FROM TrendData t WHERE "
            + "(:locationCode IS NULL OR t.locationCode = :locationCode) AND "
            + "(:tagName IS NULL OR t.tagName = :tagName) AND "
            + "(:startDate IS NULL OR t.logTime >= :startDate) AND "
            + "(:endDate IS NULL OR t.logTime <= :endDate)")
    Page<TrendData> search(@Param("locationCode") String locationCode,
                           @Param("tagName") String tagName,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate,
                           Pageable pageable);
}
