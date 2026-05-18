package com.hims.entity.repository;

import com.hims.entity.InsuranceTpaMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceTpaMappingRepository extends JpaRepository<InsuranceTpaMapping,Long> {
    List<InsuranceTpaMapping> findAllByOrderByStatusDescUpdatedAtDesc();

    List<InsuranceTpaMapping> findByStatusIgnoreCaseOrderByCreatedAtDesc(String lowerCase);
}
