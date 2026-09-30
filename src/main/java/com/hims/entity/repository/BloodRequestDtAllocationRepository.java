package com.hims.entity.repository;

import com.hims.entity.BloodComponentInventory;
import com.hims.entity.BloodRequestDt;
import com.hims.entity.BloodRequestDtAllocation;
import com.hims.entity.projection.BloodAllocatedProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BloodRequestDtAllocationRepository
        extends JpaRepository<BloodRequestDtAllocation, Long> {

    boolean existsByBloodRequestDtAndInventory(
            BloodRequestDt bloodRequestDt,
            BloodComponentInventory inventory
    );

    @Query("""
    SELECT COALESCE(SUM(a.allocatedUnits), 0)
    FROM BloodRequestDtAllocation a
    WHERE a.bloodRequestDt = :requestDt
""")
    int getAllocatedUnits(@Param("requestDt") BloodRequestDt requestDt);


    @Query(value = """
SELECT
    brh.request_hd_id AS requestHdId,
    brd.request_dt_id AS requestDtId,
    bra.allocation_id AS allocationId,
    brh.request_no AS requestNo,
    i.inpatient_id AS inpatientId,
    i.admission_no AS inpatientNo,
    p.patient_id AS patientId,
    p.p_dob AS dob,
    mg.gender_name AS gender,
    TRIM(CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)) AS patientName,
    bg.blood_group_name AS bloodGroup,
    bc.component_name AS component,
    brd.units_required AS unitsRequired,
    COALESCE((
        SELECT SUM(a2.allocated_units)
        FROM blood_request_dt_allocation a2
        WHERE a2.request_dt_id = brd.request_dt_id
        AND a2.tracking_status_id = :allocatedStatusId
    ), 0) AS unitsAllocated,
    w.ward_name AS ward,
    brd.urgency AS urgency,
    brh.created_date AS requestedOn,
    brd.required_by_datetime AS requiredBy,
    bra.tracking_status_id AS trackingStatusId,
    bci.unit_no AS unitNumber,
    bci.volume_ml AS unitVolume,
    bci.expiry_date AS unitExpiry,
    bra.inventory_id AS inventoryId
FROM blood_request_hd brh
INNER JOIN blood_request_dt brd
    ON brd.request_hd_id=brh.request_hd_id
INNER JOIN blood_request_dt_allocation bra
    ON bra.request_dt_id=brd.request_dt_id
INNER JOIN blood_component_inventory bci
    ON bci.inventory_id=bra.inventory_id
INNER JOIN patient p
    ON p.patient_id=brh.patient_id
INNER JOIN inpatient i
    ON i.inpatient_id=brh.inpatient_id
LEFT JOIN mas_ward w
    ON w.ward_id=brh.request_ward_id
LEFT JOIN mas_blood_group bg
    ON bg.blood_group_id=bci.blood_group_id
LEFT JOIN mas_blood_component bc
    ON bc.component_id=brd.component_id
LEFT JOIN mas_gender mg
    ON mg.id=p.p_gender_id
WHERE bra.tracking_status_id=:allocatedStatusId
AND bci.inventory_status=:inventoryStatusAllocated
AND bci.expiry_date>=CURRENT_TIMESTAMP
AND (
    :patientName IS NULL
    OR :patientName=''
    OR LOWER(TRIM(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln)))
       LIKE LOWER(CONCAT('%',:patientName,'%'))
)
AND (
    :wardId IS NULL
    OR brh.request_ward_id=:wardId
)
ORDER BY brd.request_dt_id DESC,bra.allocation_id DESC
""",
            countQuery = """
SELECT COUNT(*)
FROM blood_request_hd brh
INNER JOIN blood_request_dt brd
    ON brd.request_hd_id=brh.request_hd_id
INNER JOIN blood_request_dt_allocation bra
    ON bra.request_dt_id=brd.request_dt_id
INNER JOIN blood_component_inventory bci
    ON bci.inventory_id=bra.inventory_id
INNER JOIN patient p
    ON p.patient_id=brh.patient_id
INNER JOIN inpatient i
    ON i.inpatient_id=brh.inpatient_id
WHERE bra.tracking_status_id=:allocatedStatusId
AND bci.inventory_status=:inventoryStatusAllocated
AND bci.expiry_date>=CURRENT_TIMESTAMP
AND (
    :patientName IS NULL
    OR :patientName=''
    OR LOWER(TRIM(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln)))
       LIKE LOWER(CONCAT('%',:patientName,'%'))
)
AND (
    :wardId IS NULL
    OR brh.request_ward_id=:wardId
)
""",
            nativeQuery=true)
    Page<BloodAllocatedProjection> getAllocatedBloodRequestList(
            @Param("patientName") String patientName,
            @Param("wardId") Long wardId,
            @Param("allocatedStatusId") Long allocatedStatusId,
            @Param("inventoryStatusAllocated") Long inventoryStatusAllocated,
            Pageable pageable);

    List<BloodRequestDtAllocation> findByBloodRequestDt(BloodRequestDt bloodRequestDt );
}