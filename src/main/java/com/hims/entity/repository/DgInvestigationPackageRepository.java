package com.hims.entity.repository;

import com.hims.entity.DgInvestigationPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DgInvestigationPackageRepository extends JpaRepository<DgInvestigationPackage, Long> {
    List<DgInvestigationPackage> findByStatus(String status);
}
