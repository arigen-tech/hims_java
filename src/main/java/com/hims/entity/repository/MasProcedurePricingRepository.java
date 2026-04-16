package com.hims.entity.repository;

import com.hims.entity.MasProcedurePricing;
import com.hims.projection.MasProcedurePricingProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MasProcedurePricingRepository extends JpaRepository<MasProcedurePricing,Long> {

    @Query("""
    SELECT 
        p.procedurePricingId AS procedurePricingId,
        pr.procedureId AS procedureId,
        pr.procedureName AS procedureName,
        p.basePrice AS basePrice,
        p.discountAllowed AS discountAllowed,
        p.discount AS discount,
        p.effectiveFrom AS effectiveFrom,
        p.effectiveTo AS effectiveTo,
        p.status AS status,
        bt.id AS billingTypeId,
        bt.billingTypeName AS billingTypeName
    FROM MasProcedurePricing p
    LEFT JOIN p.procedure pr
    LEFT JOIN p.billingTypeId bt
    WHERE (:billingTypeId IS NULL OR bt.id = :billingTypeId)
      AND (:procedureName IS NULL OR LOWER(pr.procedureName) LIKE:procedureName )
""")
    Page<MasProcedurePricingProjection> getAllMasProcedurePricing(
            @Param("billingTypeId") Long billingTypeId,
            @Param("procedureName") String procedureName,
            Pageable pageable
    );
}
