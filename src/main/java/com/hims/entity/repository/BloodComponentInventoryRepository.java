package com.hims.entity.repository;

import com.hims.entity.BloodComponentInventory;
import com.hims.projection.BloodStockDetailedProjection;
import com.hims.projection.BloodStockSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodComponentInventoryRepository extends JpaRepository<BloodComponentInventory,Long> {
    @Query(value = """
SELECT 
    bg.blood_group_code AS bloodGroup,
    
    COUNT(bci.inventory_id) AS totalUnits,

    SUM(CASE WHEN LOWER(bc.component_code) = :component_prbc THEN 1 ELSE 0 END) AS prbc,
    SUM(CASE WHEN LOWER(bc.component_code) = :component_plt THEN 1 ELSE 0 END) AS platelets,
    SUM(CASE WHEN LOWER(bc.component_code) = :component_plasma THEN 1 ELSE 0 END) AS plasma,
    SUM(CASE WHEN LOWER(bc.component_code) = :component_cryo THEN 1 ELSE 0 END) AS cryo

FROM blood_component_inventory bci
JOIN mas_blood_group bg ON bg.blood_group_id = bci.blood_group_id
JOIN mas_blood_component bc ON bc.component_id = bci.component_id

WHERE 
(:bloodGroupId IS NULL OR bci.blood_group_id = :bloodGroupId)
AND (:componentId IS NULL OR bci.component_id = :componentId)
AND (:inventoryStatus IS NULL OR bci.inventory_status = :inventoryStatus)
AND (:collectionType IS NULL )
 AND (  :expiryFilter IS NULL
     OR (
       bci.expiry_date >= CURRENT_TIMESTAMP
      AND bci.expiry_date < CURRENT_TIMESTAMP + (
                                        
     -- number extract  (e.g. 3 from "3 days", 24 from "24 hrs")
       CAST(regexp_replace(:expiryFilter, '[^0-9]', '', 'g') AS INTEGER)
       *
   -- hrs vs days handle 
       CASE
      WHEN LOWER(:expiryFilter) LIKE '%hr%'
       THEN INTERVAL '1 hour'
      ELSE
         INTERVAL '1 day'
          END
              )
        )
    )GROUP BY bg.blood_group_code
""", nativeQuery = true)
    List<BloodStockSummaryProjection> getSummary(
            Long bloodGroupId,
            Long componentId,
            Long inventoryStatus,
            Long collectionType,
            String expiryFilter,
            String component_cryo,
            String component_plasma,
            String  component_plt,
            String component_prbc
    );

    @Query(value = """
SELECT 
    bci.unit_no AS unitNo,
    bc.component_code AS component,
    bg.blood_group_code AS bloodGroup,
    bci.volume_ml AS volumeMl,
    bci.expiry_date AS expiryDate,
    ms.donation_status_code AS status

FROM blood_component_inventory bci
JOIN mas_blood_component bc ON bc.component_id = bci.component_id
JOIN mas_blood_group bg ON bg.blood_group_id = bci.blood_group_id
JOIN mas_blood_donation_status ms ON ms.donation_status_id = bci.inventory_status

WHERE 
(:bloodGroupId IS NULL OR bci.blood_group_id = :bloodGroupId)
AND (:componentId IS NULL OR bci.component_id = :componentId)
AND (:inventoryStatus IS NULL OR bci.inventory_status = :inventoryStatus)
AND (:collectionType IS NULL )

AND (  :expiryFilter IS NULL
     OR (
       bci.expiry_date >= CURRENT_TIMESTAMP
      AND bci.expiry_date < CURRENT_TIMESTAMP + (
                                        
     -- number extract  (e.g. 3 from "3 days", 24 from "24 hrs")
       CAST(regexp_replace(:expiryFilter, '[^0-9]', '', 'g') AS INTEGER)
       *
   -- hrs vs days handle 
       CASE
      WHEN LOWER(:expiryFilter) LIKE '%hr%'
       THEN INTERVAL '1 hour'
      ELSE
         INTERVAL '1 day'
          END
              )
        )
    )

ORDER BY bci.expiry_date ASC
""", nativeQuery = true)
    List<BloodStockDetailedProjection> getDetailed(
            Long bloodGroupId,
            Long componentId,
            Long inventoryStatus,
            Long collectionType,
            String expiryFilter

    );
}
