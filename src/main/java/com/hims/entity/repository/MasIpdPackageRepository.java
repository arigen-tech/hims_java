package com.hims.entity.repository;

import com.hims.entity.MasIpdPackage;
import com.hims.projection.IpdPackageDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasIpdPackageRepository extends JpaRepository<MasIpdPackage,Long> {
    List<MasIpdPackage> findByStatusIgnoreCaseOrderByPackageNameAsc(String lowerCase);

    List<MasIpdPackage> findAllByOrderByStatusDescLastChgDateDesc();

    @Query(value = """
    SELECT 
        p.package_id AS packageId,
        p.package_name AS packageName,
        ac.admission_category_name AS type,
        d.department_name AS departmentName,
        p.stay_days AS stayDays,
        p.generated_inclusions AS generatedInclusions,
        p.generated_exclusions AS generatedExclusions,
        p.last_chg_date AS lastChgDate,
        p.status AS status,

        i.inclusion_id AS inclusionId,
        sc.category_id AS serviceCategoryId,
        sc.category_name AS serviceCategoryName,
        i.limit_amount AS limitAmount,
        i.included_flag As inclusionFlag,
        i.limit_qty AS limitQty

    FROM mas_ipd_package p
    LEFT JOIN mas_admission_category ac ON p.package_type_id = ac.admission_category_id
    LEFT JOIN mas_department d ON p.dept_id = d.department_id
    LEFT JOIN mas_ipd_package_inclusion i 
        ON p.package_id = i.package_id AND i.status =:status
    LEFT JOIN mas_ipd_service_category sc 
        ON i.service_category_id = sc.category_id
 WHERE p.package_id = :packageId
""", nativeQuery = true)
    List<IpdPackageDetailsProjection> getPackageDetails(@Param("packageId") Long packageId,
                                                        @Param("status") String status);
}
