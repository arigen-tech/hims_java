package com.hims.entity.repository;

import com.hims.entity.BloodRequestDt;
import com.hims.projection.BloodTrackingProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloodRequestDtRepository extends JpaRepository<BloodRequestDt, Long> {

    @Query(value = """
SELECT
    brd.request_dt_id AS requestDtId,
    brh.request_no AS requestNo,
    i.inpatient_id AS inpatientId,
    i.admission_no AS inpatientNo,
    p.patient_id AS patientId,
    TRIM(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln)) AS patientName,
    bg.blood_group_name AS bloodGroup,
    bg.blood_group_id AS bloodGroupId,
    bc.component_name AS component,
    bc.component_id AS componentId,
    brd.units_required AS units,

    COALESCE((
        SELECT SUM(a.allocated_units)
        FROM blood_request_dt_allocation a
        WHERE a.request_dt_id=brd.request_dt_id
    ),0) AS allocatedUnits,

    (
        SELECT ARRAY_AGG(a.allocation_id ORDER BY a.allocation_id)
        FROM blood_request_dt_allocation a
        WHERE a.request_dt_id=brd.request_dt_id
        AND a.tracking_status_id=:issuedStatusId
    ) AS allocationIds,

    COALESCE(brd.fulfilled_units,0) AS fulfilledUnits,

    COALESCE((
        SELECT COUNT(*)
        FROM blood_crossmatch_failed_history bfh
        WHERE bfh.blood_detail_id=brd.request_dt_id
    ),0) AS failedUnits,

    GREATEST(
        brd.units_required
        - COALESCE(brd.fulfilled_units,0)
        - COALESCE((
            SELECT SUM(a.allocated_units)
            FROM blood_request_dt_allocation a
            WHERE a.request_dt_id=brd.request_dt_id
            AND a.tracking_status_id NOT IN(
                :crossmatchFailedStatusId,
                :rejectedStatusId
            )
        ),0)
        + COALESCE((
            SELECT SUM(a.allocated_units)
            FROM blood_request_dt_allocation a
            WHERE a.request_dt_id=brd.request_dt_id
            AND a.tracking_status_id=:issuedStatusId
        ),0),
        0
    ) AS pendingUnits,

    COALESCE((
        SELECT COUNT(*)
        FROM blood_request_acknowledgement ack
        INNER JOIN blood_request_dt_allocation a
            ON a.allocation_id=ack.allocation_id
        WHERE a.request_dt_id=brd.request_dt_id
        AND LOWER(ack.acknowledgement_status)
            =LOWER(:acknowledgementAcceptedStatus)
    ),0) AS acknowledgedUnits,

    GREATEST(
        COALESCE(brd.fulfilled_units,0)
        - COALESCE((
            SELECT COUNT(*)
            FROM blood_request_acknowledgement ack
            INNER JOIN blood_request_dt_allocation a
                ON a.allocation_id=ack.allocation_id
            WHERE a.request_dt_id=brd.request_dt_id
            AND LOWER(ack.acknowledgement_status)
                =LOWER(:acknowledgementAcceptedStatus)
        ),0),
        0
    ) AS pendingAcknowledgementUnits,

    CASE
        WHEN COALESCE(brd.fulfilled_units,0)
        >
        COALESCE((
            SELECT COUNT(*)
            FROM blood_request_acknowledgement ack
            INNER JOIN blood_request_dt_allocation a
                ON a.allocation_id=ack.allocation_id
            WHERE a.request_dt_id=brd.request_dt_id
            AND LOWER(ack.acknowledgement_status)
                =LOWER(:acknowledgementAcceptedStatus)
        ),0)
        THEN TRUE
        ELSE FALSE
    END AS canAcknowledge,

    brd.urgency AS urgency,
    brh.request_datetime AS requestedDateTime,
    brd.required_by_datetime AS requiredByDateTime,
    mw.ward_name AS requestedWard,
    brh.requested_by AS requestedBy,

    CASE
        WHEN COALESCE(brd.fulfilled_units,0)>=brd.units_required
            THEN issuedStatus.status_code

        WHEN COALESCE(brd.fulfilled_units,0)>0
            THEN partiallyIssuedStatus.status_code

        WHEN EXISTS(
            SELECT 1
            FROM blood_request_dt_allocation a
            WHERE a.request_dt_id=brd.request_dt_id
            AND a.tracking_status_id=:crossmatchCompletedStatusId
        )
            THEN crossmatchCompletedStatus.status_code

        WHEN EXISTS(
            SELECT 1
            FROM blood_request_dt_allocation a
            WHERE a.request_dt_id=brd.request_dt_id
            AND a.tracking_status_id=:allocatedStatusId
        )
            THEN allocatedStatus.status_code

        WHEN EXISTS(
            SELECT 1
            FROM blood_request_dt_allocation a
            WHERE a.request_dt_id=brd.request_dt_id
            AND a.tracking_status_id=:crossmatchFailedStatusId
        )
            THEN crossmatchFailedStatus.status_code

        ELSE btsm.status_code
    END AS trackingStatus

FROM blood_request_dt brd

LEFT JOIN blood_request_hd brh
    ON brh.request_hd_id=brd.request_hd_id

LEFT JOIN inpatient i
    ON i.inpatient_id=brh.inpatient_id

LEFT JOIN patient p
    ON p.patient_id=brh.patient_id

LEFT JOIN mas_blood_group bg
    ON bg.blood_group_id=brh.blood_group_id

LEFT JOIN mas_blood_component bc
    ON bc.component_id=brd.component_id

LEFT JOIN mas_ward mw
    ON mw.ward_id=brh.request_ward_id

LEFT JOIN blood_tracking_status_master btsm
    ON btsm.status_id=brd.tracking_status_id

LEFT JOIN blood_tracking_status_master issuedStatus
    ON issuedStatus.status_id=:issuedStatusId

LEFT JOIN blood_tracking_status_master partiallyIssuedStatus
    ON partiallyIssuedStatus.status_id=:partiallyIssuedStatusId

LEFT JOIN blood_tracking_status_master crossmatchCompletedStatus
    ON crossmatchCompletedStatus.status_id=:crossmatchCompletedStatusId

LEFT JOIN blood_tracking_status_master allocatedStatus
    ON allocatedStatus.status_id=:allocatedStatusId

LEFT JOIN blood_tracking_status_master crossmatchFailedStatus
    ON crossmatchFailedStatus.status_id=:crossmatchFailedStatusId
    
 WHERE
    (
        :inpatientId IS NULL
        OR brh.inpatient_id=:inpatientId 
    )   

AND
    (
        :inpatientNo IS NULL
        OR :inpatientNo=''
        OR LOWER(i.admission_no)
            LIKE LOWER(CONCAT('%',:inpatientNo,'%'))
    )

AND (
        :patientName IS NULL
        OR :patientName=''
        OR LOWER(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln))
            LIKE LOWER(CONCAT('%',:patientName,'%'))
    )

AND (
        :requestNo IS NULL
        OR :requestNo=''
        OR LOWER(brh.request_no)
            LIKE LOWER(CONCAT('%',:requestNo,'%'))
    )

ORDER BY brd.request_dt_id DESC
""",
            countQuery = """
SELECT COUNT(*)
FROM blood_request_dt brd

LEFT JOIN blood_request_hd brh
    ON brh.request_hd_id=brd.request_hd_id

LEFT JOIN inpatient i
    ON i.inpatient_id=brh.inpatient_id

LEFT JOIN patient p
    ON p.patient_id=brh.patient_id

 WHERE
    (
        :inpatientId IS NULL
        OR brh.inpatient_id=:inpatientId 
    )   
AND
    (
        :inpatientNo IS NULL
        OR :inpatientNo=''
        OR LOWER(i.admission_no)
            LIKE LOWER(CONCAT('%',:inpatientNo,'%'))
    )

AND (
        :patientName IS NULL
        OR :patientName=''
        OR LOWER(CONCAT_WS(' ',p.p_fn,p.p_mn,p.p_ln))
            LIKE LOWER(CONCAT('%',:patientName,'%'))
    )

AND (
        :requestNo IS NULL
        OR :requestNo=''
        OR LOWER(brh.request_no)
            LIKE LOWER(CONCAT('%',:requestNo,'%'))
    )
""",
            nativeQuery = true)
    Page<BloodTrackingProjection> getBloodRequestTrackingList(
            @Param("inpatientId")Long inpatientId,
            @Param("inpatientNo") String inpatientNo,
            @Param("patientName") String patientName,
            @Param("requestNo") String requestNo,
            @Param("issuedStatusId") Long issuedStatusId,
            @Param("partiallyIssuedStatusId") Long partiallyIssuedStatusId,
            @Param("allocatedStatusId") Long allocatedStatusId,
            @Param("crossmatchCompletedStatusId") Long crossmatchCompletedStatusId,
            @Param("crossmatchFailedStatusId") Long crossmatchFailedStatusId,
            @Param("rejectedStatusId") Long rejectedStatusId,
            @Param("acknowledgementAcceptedStatus") String acknowledgementAcceptedStatus,
            Pageable pageable);


    @Query(value = """
    SELECT
    brd.request_dt_id AS requestDtId,
    brh.request_no AS requestNo,
    i.inpatient_id AS inpatientId,
    i.admission_no AS inpatientNo,
    p.patient_id AS patientId,
    TRIM(CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)) AS patientName,
    bg.blood_group_name AS bloodGroup,
    bg.blood_group_id AS bloodGroupId,
    bc.component_name AS component,
    bc.component_id AS componentId,
    brd.units_required AS units,

    COALESCE((
        SELECT SUM(a.allocated_units)
        FROM blood_request_dt_allocation a
        WHERE a.request_dt_id = brd.request_dt_id
        AND a.tracking_status_id NOT IN (
            :crossmatchFailedStatusId,
            :rejectedStatusId
        )
    ), 0) AS allocatedUnits,

    COALESCE(brd.fulfilled_units, 0) AS fulfilledUnits,

    COALESCE((
        SELECT SUM(a.allocated_units)
        FROM blood_request_dt_allocation a
        WHERE a.request_dt_id = brd.request_dt_id
        AND a.tracking_status_id = :crossmatchFailedStatusId
    ), 0) AS failedUnits,

    GREATEST(
        brd.units_required -
        COALESCE((
            SELECT SUM(a.allocated_units)
            FROM blood_request_dt_allocation a
            WHERE a.request_dt_id = brd.request_dt_id
            AND a.tracking_status_id NOT IN (
                :crossmatchFailedStatusId,
                :rejectedStatusId
            )
        ), 0),
        0
    ) AS pendingUnits,

    brd.urgency AS urgency,
    brh.request_datetime AS requestedDateTime,
    brh.requested_by AS requestedBy,
    brd.required_by_datetime AS requiredByDateTime,
    mw.ward_name AS requestedWard,
    btsm.status_code AS trackingStatus

FROM blood_request_dt brd

LEFT JOIN blood_request_hd brh
    ON brh.request_hd_id = brd.request_hd_id

LEFT JOIN inpatient i
    ON i.inpatient_id = brh.inpatient_id

LEFT JOIN patient p
    ON p.patient_id = brh.patient_id

LEFT JOIN mas_blood_group bg
    ON bg.blood_group_id = brh.blood_group_id

LEFT JOIN mas_blood_component bc
    ON bc.component_id = brd.component_id

LEFT JOIN mas_ward mw
    ON mw.ward_id = brh.request_ward_id

LEFT JOIN blood_tracking_status_master btsm
    ON btsm.status_id = brd.tracking_status_id

WHERE
    (
        :patientName IS NULL
        OR :patientName = ''
        OR LOWER(
            TRIM(CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln))
        ) LIKE LOWER(CONCAT('%', :patientName, '%'))
    )

AND
    (
        :wardId IS NULL
        OR brh.request_ward_id = :wardId
    )

AND
    (
        brd.units_required >
        COALESCE((
            SELECT SUM(a.allocated_units)
            FROM blood_request_dt_allocation a
            WHERE a.request_dt_id = brd.request_dt_id
            AND a.tracking_status_id NOT IN (
                :crossmatchFailedStatusId,
                :rejectedStatusId
            )
        ), 0)
    )

ORDER BY brd.request_dt_id DESC
""",
            countQuery = """
SELECT COUNT(*)
FROM blood_request_dt brd

LEFT JOIN blood_request_hd brh
    ON brh.request_hd_id = brd.request_hd_id

LEFT JOIN patient p
    ON p.patient_id = brh.patient_id

WHERE
    (
        :patientName IS NULL
        OR :patientName = ''
        OR LOWER(
            TRIM(CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln))
        ) LIKE LOWER(CONCAT('%', :patientName, '%'))
    )

AND
    (
        :wardId IS NULL
        OR brh.request_ward_id = :wardId
    )

AND
    (
        brd.units_required >
        COALESCE((
            SELECT SUM(a.allocated_units)
            FROM blood_request_dt_allocation a
            WHERE a.request_dt_id = brd.request_dt_id
            AND a.tracking_status_id NOT IN (
                :crossmatchFailedStatusId,
                :rejectedStatusId
            )
        ), 0)
    )
""",
            nativeQuery = true)
    Page<BloodTrackingProjection> getAllPendingBloodRequests(
            @Param("patientName") String patientName,
            @Param("wardId") Long wardId,
            @Param("crossmatchFailedStatusId") Long crossmatchFailedStatusId,
            @Param("rejectedStatusId") Long rejectedStatusId,
            Pageable pageable);

}
