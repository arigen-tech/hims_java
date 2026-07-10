package com.hims.entity.repository;

import com.hims.entity.PackageRateConfig;
import com.hims.projection.PackageRateConfigProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRateConfigRepository extends JpaRepository<PackageRateConfig,Long> {
    @Query("""
        SELECT 
            p.configId AS configId,

            pkg.packageId AS packageId,
            pkg.packageName AS packageName,

            bt.billingTypeId AS billingTypeId,
            bt.billingTypeName AS billingTypeName,

            ins.insuranceId AS insuranceId,
            ins.insuranceName AS insuranceName,

            t.tpaId AS tpaId,
            t.tpaName AS tpaName,

            c.corporateId AS corporateId,
            c.corporateName AS corporateName,

            r.roomCategoryId AS roomCategoryId,
            r.roomCategoryName AS roomCategoryName,

            p.amount AS amount,
            p.effectiveFrom AS effectiveFrom,
            p.effectiveTo AS effectiveTo,

            p.preauthRequired AS preAuthRequired,

            p.copayPercent AS copayPercent,
            p.maxClaimAmount AS maxClaimAmount,

            p.status AS status

        FROM PackageRateConfig p
        LEFT JOIN p.ipdPackage pkg
        LEFT JOIN p.billingType bt
        LEFT JOIN p.insuranceId ins
        LEFT JOIN p.tpa t
        LEFT JOIN p.corporate c
        LEFT JOIN p.masRoomCategory r

        WHERE (:billingTypeId IS NULL OR bt.billingTypeId = :billingTypeId)
          AND (:corporateId IS NULL OR c.corporateId = :corporateId)
          AND (:insuranceId IS NULL OR ins.insuranceId = :insuranceId)

          AND (
                :packageName IS NULL OR
                LOWER(pkg.packageName) LIKE :packageName
              )
    """)
    Page<PackageRateConfigProjection> getByAllPackageRateConfigId(
            @Param("billingTypeId") Long billingTypeId,
            @Param("corporateId") Long corporateId,
            @Param("insuranceId") Long insuranceId,
            @Param("packageName") String packageName,
            Pageable pageable
    );

    @Query("""
    SELECT 
        p.configId AS configId,

        pkg.packageId AS packageId,
        pkg.packageName AS packageName,

        bt.billingTypeId AS billingTypeId,
        bt.billingTypeName AS billingTypeName,

        ins.insuranceId AS insuranceId,
        ins.insuranceName AS insuranceName,

        t.tpaId AS tpaId,
        t.tpaName AS tpaName,

        c.corporateId AS corporateId,
        c.corporateName AS corporateName,

        r.roomCategoryId AS roomCategoryId,
        r.roomCategoryName AS roomCategoryName,

        p.amount AS amount,
        p.effectiveFrom AS effectiveFrom,
        p.effectiveTo AS effectiveTo,

        p.preauthRequired AS preAuthRequired,

        p.copayPercent AS copayPercent,
        p.maxClaimAmount AS maxClaimAmount,

        p.status AS status

    FROM PackageRateConfig p
    LEFT JOIN p.ipdPackage pkg
    LEFT JOIN p.billingType bt
    LEFT JOIN p.insuranceId ins
    LEFT JOIN p.tpa t
    LEFT JOIN p.corporate c
    LEFT JOIN p.masRoomCategory r

    WHERE p.configId = :id
""")
    PackageRateConfigProjection getByIdPackageRateConfig(@Param("id") Long id);
}
