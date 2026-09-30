package com.hims.entity.repository;

import com.hims.entity.BloodRequestHd;
import com.hims.entity.projection.BloodIssueProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloodRequestHdRepository extends JpaRepository<BloodRequestHd, Long> {

    @Query(value = """
SELECT
    brh.request_hd_id AS requestHdId,
    brd.request_dt_id AS requestDtId,
    bra.allocation_id AS allocationId,
    brh.request_no AS requestNo,
    i.admission_no AS inpatientNo,
    TRIM(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln)) AS patientName,
    mbg.blood_group_name AS bloodGroup,
    mbc.component_name AS component,
    bra.allocated_units AS unitsReserved,
    mw.ward_name AS requestDept,
    brd.urgency AS urgency,
    brd.required_by_datetime AS requiredBy,
    bra.inventory_id AS inventoryId,
    bra.allocated_date AS reservedOn
FROM blood_request_hd brh
JOIN blood_request_dt brd
    ON brd.request_hd_id=brh.request_hd_id
JOIN blood_request_dt_allocation bra
    ON bra.request_dt_id=brd.request_dt_id
JOIN blood_crossmatch_hd bch
    ON bch.request_id=brh.request_hd_id
JOIN blood_crossmatch_dt bcd
    ON bcd.crossmatch_hd_id=bch.crossmatch_hd_id
    AND bcd.inventory_id=bra.inventory_id
JOIN blood_component_inventory bci
    ON bci.inventory_id=bra.inventory_id
JOIN inpatient i
    ON i.inpatient_id=brh.inpatient_id
JOIN patient p
    ON p.patient_id=brh.patient_id
JOIN mas_blood_group mbg
    ON mbg.blood_group_id=brh.blood_group_id
JOIN mas_blood_component mbc
    ON mbc.component_id=brd.component_id
LEFT JOIN mas_ward mw
    ON mw.ward_id=brh.request_ward_id
WHERE LOWER(bcd.compatibility_result)=LOWER(:bloodIssueCompatibleStatus)
AND bra.tracking_status_id=:crossmatchCompletedStatusId
AND bci.inventory_status=:inventoryStatusReserved
AND (:requestNo IS NULL OR LOWER(brh.request_no) LIKE LOWER(CONCAT('%',:requestNo,'%')))
AND (:patientName IS NULL OR LOWER(TRIM(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln))) LIKE LOWER(CONCAT('%',:patientName,'%')))
AND (:wardId IS NULL OR brh.request_ward_id=:wardId)
ORDER BY bra.allocated_date DESC
""",
            countQuery = """
SELECT COUNT(*)
FROM blood_request_dt_allocation bra
JOIN blood_request_dt brd
    ON brd.request_dt_id=bra.request_dt_id
JOIN blood_request_hd brh
    ON brh.request_hd_id=brd.request_hd_id
JOIN blood_crossmatch_hd bch
    ON bch.request_id=brh.request_hd_id
JOIN blood_crossmatch_dt bcd
    ON bcd.crossmatch_hd_id=bch.crossmatch_hd_id
    AND bcd.inventory_id=bra.inventory_id
JOIN blood_component_inventory bci
    ON bci.inventory_id=bra.inventory_id
JOIN patient p
    ON p.patient_id=brh.patient_id
WHERE LOWER(bcd.compatibility_result)=LOWER(:bloodIssueCompatibleStatus)
AND bra.tracking_status_id=:crossmatchCompletedStatusId
AND bci.inventory_status=:inventoryStatusReserved
AND (:requestNo IS NULL OR LOWER(brh.request_no) LIKE LOWER(CONCAT('%',:requestNo,'%')))
AND (:patientName IS NULL OR LOWER(TRIM(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln))) LIKE LOWER(CONCAT('%',:patientName,'%')))
AND (:wardId IS NULL OR brh.request_ward_id=:wardId)
""",
            nativeQuery = true)
    Page<BloodIssueProjection> getPendingBloodIssue(
            @Param("requestNo") String requestNo,
            @Param("patientName") String patientName,
            @Param("wardId") Long wardId,
            @Param("crossmatchCompletedStatusId") Long crossmatchCompletedStatusId,
            @Param("inventoryStatusReserved") Long inventoryStatusReserved,
            @Param("bloodIssueCompatibleStatus") String bloodIssueCompatibleStatus,
            Pageable pageable);

}
