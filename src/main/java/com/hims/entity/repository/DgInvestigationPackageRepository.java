package com.hims.entity.repository;

import com.hims.entity.DgInvestigationPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DgInvestigationPackageRepository extends JpaRepository<DgInvestigationPackage, Long> {
    List<DgInvestigationPackage> findByStatus(String status);

    @Query(value = """
    SELECT id, name, actual_cost, category
    FROM dg_investigation_package
    WHERE name = :packName
      AND :currDate BETWEEN from_dt AND to_dt
      AND status = 'y'
""", nativeQuery = true)
    List<Object[]> findActivePackageByNameAndDateRaw(
            @Param("packName") String packName,
            @Param("currDate") LocalDate currDate
    );

}
