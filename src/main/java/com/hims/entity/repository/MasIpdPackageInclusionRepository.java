package com.hims.entity.repository;

import com.hims.entity.MasIpdPackageInclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasIpdPackageInclusionRepository extends JpaRepository<MasIpdPackageInclusion,Long> {
    List<MasIpdPackageInclusion> findByMasIpdPackage_PackageIdAndStatusIgnoreCase(Long id, String lowerCase);

    List<MasIpdPackageInclusion> findByMasIpdPackage_PackageId(Long id);
}
