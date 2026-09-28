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


    @Query(value = """
SELECT
brh.request_hd_id AS requestHdId,
brd.request_dt_id AS requestDtId,
brh.request_no AS requestNo,
i.inpatient_id AS inpatientId,
i.admission_no AS inpatientNo,
p.patient_id AS patientId,
p.p_dob AS dob,
mg.gender_name AS gender,
TRIM(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln)) AS patientName,
bg.blood_group_name AS bloodGroup,
bc.component_name AS component,
brd.units_required AS unitsRequired,
COALESCE(SUM(bra.allocated_units),0) AS unitsAllocated,
w.ward_name AS ward,
brd.urgency AS urgency,
brh.created_date AS requestedOn,
brd.required_by_datetime AS requiredBy,
brd.tracking_status_id AS trackingStatusId,
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
WHERE brd.tracking_status_id IN(:allocatedStatusId,:partiallyAllocatedStatusId)
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
GROUP BY
brh.request_hd_id,
brd.request_dt_id,
brh.request_no,
i.inpatient_id,
i.admission_no,
p.patient_id,
p.p_dob,
mg.gender_name,
p.p_fn,
p.p_mn,
p.p_ln,
bg.blood_group_name,
bc.component_name,
brd.units_required,
w.ward_name,
brd.urgency,
brh.created_date,
brd.required_by_datetime,
brd.tracking_status_id,
bci.unit_no,
bci.volume_ml,
bci.expiry_date,
bra.inventory_id
ORDER BY brd.request_dt_id DESC
""",
            countQuery = """
SELECT COUNT(DISTINCT brd.request_dt_id)
FROM blood_request_hd brh
INNER JOIN blood_request_dt brd
ON brd.request_hd_id=brh.request_hd_id
INNER JOIN blood_request_dt_allocation bra
ON bra.request_dt_id=brd.request_dt_id
INNER JOIN patient p
ON p.patient_id=brh.patient_id
INNER JOIN inpatient i
ON i.inpatient_id=brh.inpatient_id
WHERE brd.tracking_status_id IN(:allocatedStatusId,:partiallyAllocatedStatusId)
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
            nativeQuery = true)
    Page<BloodAllocatedProjection> getAllocatedBloodRequestList(
            @Param("patientName") String patientName,
            @Param("wardId") Long wardId,
            @Param("allocatedStatusId") Long allocatedStatusId,
            @Param("partiallyAllocatedStatusId") Long partiallyAllocatedStatusId,
            Pageable pageable);

    List<BloodRequestDtAllocation> findByBloodRequestDt(BloodRequestDt bloodRequestDt );
}