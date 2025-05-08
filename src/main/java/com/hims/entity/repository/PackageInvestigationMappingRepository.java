package com.hims.entity.repository;

import com.hims.entity.DgInvestigationPackage;
import com.hims.entity.PackageInvestigationMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageInvestigationMappingRepository extends JpaRepository <PackageInvestigationMapping, Long> {
    List<PackageInvestigationMapping> findByStatus(String status);


    List<PackageInvestigationMapping> findByPackageId(DgInvestigationPackage packag);
}
