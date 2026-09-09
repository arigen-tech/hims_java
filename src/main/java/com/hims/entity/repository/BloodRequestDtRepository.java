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
             i.inpatient_id AS inpatientId,
            i.admission_no AS inpatientNo,
            p.patient_id AS patientId,
            TRIM(CONCAT_WS(' ',  p.p_fn,  p.p_mn,  p.p_ln )) AS patientName,
            bg.blood_group_name AS bloodGroup,
            bc.component_name AS component,
            brd.units_required AS units,
            brd.urgency AS urgency,
            brh.request_datetime AS requestedDateTime,
            brh.requested_by AS requestedBy,
            brd.detail_status AS trackingStatus
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
        WHERE
            (
                :inpatientNo IS NULL
                OR :inpatientNo = ''
                OR LOWER(i.admission_no)
                    LIKE LOWER(CONCAT('%', :inpatientNo, '%'))
            )

        AND
            (
                :patientName IS NULL
                OR :patientName = ''
                OR LOWER(
                    CONCAT_WS(' ',
                        p.p_fn,
                        p.p_mn,
                        p.p_ln
                    )
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
            )

        ORDER BY brd.request_dt_id DESC
        """,

            countQuery = """
        SELECT COUNT(*)
        FROM blood_request_dt brd

        LEFT JOIN blood_request_hd brh
            ON brh.request_hd_id = brd.request_hd_id

        LEFT JOIN inpatient i
            ON i.inpatient_id = brh.inpatient_id

        LEFT JOIN patient p
            ON p.patient_id = brh.patient_id

        WHERE
            (
                :inpatientNo IS NULL
                OR :inpatientNo = ''
                OR LOWER(i.admission_no)
                    LIKE LOWER(CONCAT('%', :inpatientNo, '%'))
            )

        AND
            (
                :patientName IS NULL
                OR :patientName = ''
                OR LOWER(
                    CONCAT_WS(' ',
                        p.p_fn,
                        p.p_mn,
                        p.p_ln
                    )
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
            )
        """,

            nativeQuery = true
    )
    Page<BloodTrackingProjection> getBloodRequestTrackingList(
            @Param("inpatientNo") String inpatientNo,
            @Param("patientName") String patientName,
            Pageable pageable
    );
}
