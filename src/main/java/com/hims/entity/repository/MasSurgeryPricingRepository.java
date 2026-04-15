package com.hims.entity.repository;

import com.hims.entity.MasSurgeryPricing;
import com.hims.projection.MasSurgeryPricingProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MasSurgeryPricingRepository extends JpaRepository<MasSurgeryPricing,Long> {
    @Query("""
SELECT 
    p.surgeryPricingId AS surgeryPricingId,
    s.surgeryId AS surgeryId,
    s.surgeryName AS surgeryName,
    p.amount AS amount,
    p.discountAllowed AS discountAllowed,
    p.effectiveFrom AS effectiveFrom,
    p.effectiveTo AS effectiveTo,
    p.status AS status,
    bt.billingTypeId AS billingTypeId,
    bt.billingTypeName AS billingTypeName
FROM MasSurgeryPricing p
LEFT JOIN p.surgery s
LEFT JOIN p.billingType bt
WHERE (:billingTypeId IS NULL OR bt.billingTypeId = :billingTypeId)
AND (:surgeryName IS NULL OR LOWER(s.surgeryName) LIKE :surgeryName)
""")
    Page<MasSurgeryPricingProjection> getAllMasSurgeryPricing(
            @Param("billingTypeId") Long billingTypeId,
            @Param("surgeryName") String surgeryName,
            Pageable pageable
    );
}
