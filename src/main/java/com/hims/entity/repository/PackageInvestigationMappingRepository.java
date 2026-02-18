package com.hims.entity.repository;

import com.hims.entity.DgInvestigationPackage;
import com.hims.entity.PackageInvestigationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PackageInvestigationMappingRepository extends JpaRepository <PackageInvestigationMapping, Long> {
    List<PackageInvestigationMapping> findByStatus(String status);


    List<PackageInvestigationMapping> findByPackageId(DgInvestigationPackage packag);

    @Query("SELECT DISTINCT m.packageId FROM PackageInvestigationMapping m WHERE (:status IS NULL OR m.status = :status)")
    List<DgInvestigationPackage> findDistinctPackages(@Param("status") String status);

    List<PackageInvestigationMapping> findByPackageIdPackIdAndStatus(Long packageId, String y);

    Optional<PackageInvestigationMapping> findByPackageIdPackIdAndInvestIdInvestigationId(Long packageId, Long invId);

    List<PackageInvestigationMapping> findByPackageIdPackId(Long packageId);
}
