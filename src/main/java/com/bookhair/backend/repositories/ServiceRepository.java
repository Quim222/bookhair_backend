package com.bookhair.backend.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bookhair.backend.model.Services;

@Repository
public interface ServiceRepository extends JpaRepository<Services, String>, JpaSpecificationExecutor<Services> {
        List<Services> findAllByNameContainingIgnoreCase(String name);

        List<Services> findByDurationBetweenAndPriceBetween(
                        int minDuration, int maxDuration,
                        BigDecimal minPrice, BigDecimal maxPrice);

        boolean existsByNameIgnoreCase(String name);

}
