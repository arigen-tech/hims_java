package com.hims.entity.repository;

import com.hims.entity.Inpatient;
import com.hims.projection.*;
import lombok.Locked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InpatientRepository extends JpaRepository<Inpatient,Long> {

    @Query(
            value = """
        SELECT admission_no
        FROM inpatient
        WHERE admission_no LIKE :financialYearPattern
        ORDER BY CAST(SPLIT_PART(admission_no, '/', 3) AS INTEGER) DESC
        LIMIT 1
        """,
            nativeQuery = true
    )
    String findLastAdmissionNoByFinancialYear(
            @Param("financialYearPattern") String financialYearPattern
    );

    Optional<Inpatient> findTopByPatient_IdOrderByInpatientIdDesc(Long patientId);

    @Query("""
            SELECT i
            FROM Inpatient i
            JOIN IpdBillingHeader bh ON bh.inpatientId = i
            LEFT JOIN FETCH i.admittingWardId
            LEFT JOIN FETCH i.room
            WHERE i.admissionStatus.admissionStatusId = :admissionStatus
            """)
    List<Inpatient> findAdmittedInpatients(@Param("admissionStatus") Long admissionStatus);

    @Query(value = """
SELECT
    i.inpatient_id   AS inpatientId,
    bh.bill_id  AS billingHeaderId,
    p.uhid_no    AS uhid,
    CONCAT(
        COALESCE(p.p_fn,''),' ',
        COALESCE(p.p_mn,''),' ',
        COALESCE(p.p_ln,'')
    )     AS patientName,
    p.p_age   AS age,
    g.id AS genderId,
    g.gender_name  AS gender,
    p.p_mobile_number AS mobileNo,
    i.admission_no AS admissionNo,
    w.ward_id  AS wardId,
    w.ward_name  AS ward,
    r.room_id  AS roomId,
    r.room_name  AS room,
    b.bed_id AS bedId,
    b.bed_number AS bed,
    (i.admission_date + i.admission_time) AS admissionDateTime,
    bt.billing_type_id AS billingTypeId,
    bt.billing_type_name AS billingType
FROM inpatient i
INNER JOIN patient p
        ON p.patient_id = i.patient
LEFT JOIN mas_gender g
       ON g.id = p.p_gender_id
LEFT JOIN mas_ward w
       ON w.ward_id = i.admitting_ward_id
LEFT JOIN mas_room r
       ON r.room_id = i.room_id
LEFT JOIN mas_bed b
       ON b.bed_id = i.bed_id
LEFT JOIN ipd_billing_header bh
       ON bh.inpatient_id = i.inpatient_id
LEFT JOIN mas_ipd_billing_type bt
       ON bt.billing_type_id = bh.billing_type_id
WHERE i.admission_status = :admissionStatus
AND (
        :patientName IS NULL
        OR LOWER(CONCAT(
                COALESCE(p.p_fn,''),' ',
                COALESCE(p.p_mn,''),' ',
                COALESCE(p.p_ln,'')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
)
AND (
        :mobileNo IS NULL
        OR p.p_mobile_number ILIKE CONCAT('%', :mobileNo, '%')
)
AND (
        :admissionNo IS NULL
        OR i.admission_no ILIKE CONCAT('%', :admissionNo, '%')
)
ORDER BY i.inpatient_id DESC
""",
            countQuery = """
SELECT COUNT(*)
FROM inpatient i
INNER JOIN patient p
        ON p.patient_id = i.patient
WHERE i.admission_status = :admissionStatus
AND (
        :patientName IS NULL
        OR LOWER(CONCAT(
                COALESCE(p.p_fn,''),' ',
                COALESCE(p.p_mn,''),' ',
                COALESCE(p.p_ln,'')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
)
AND (
        :mobileNo IS NULL
        OR p.p_mobile_number ILIKE CONCAT('%', :mobileNo, '%')
)
AND (
        :admissionNo IS NULL
        OR i.admission_no ILIKE CONCAT('%', :admissionNo, '%')
)
""",
            nativeQuery = true)
    Page<InpatientAdvanceCollectionProjection> getIpdAdvanceCollection(
            @Param("admissionStatus") Long admissionStatus,
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("admissionNo") String admissionNo,
            Pageable pageable);



        @Query("""
        SELECT
            p.id AS patientId,
            p.patientFn AS patientFn,
            p.patientMn AS patientMn,
            p.patientLn AS patientLn,
            p.uhidNo AS uhidNo,
            p.patientAge AS patientAge,
            g.id AS genderId,
            g.genderName AS genderName,
            p.patientMobileNumber AS patientMobileNumber,
             p.emerMobile AS emergencyContactNo,

            i.admissionNo AS admissionNo,
            i.admissionDate AS admissionDate,
            i.admissionTime AS admissionTime,
            ac.admissionCategoryName AS admissionCategoryName,
            at.admissionTypeName AS admissionTypeName,
            asrc.admissionSourceName AS admissionSourceName,
            ast.statusCode AS admissionStatusName,
            i.dischargeDate AS dischargeDate,

            i.doctorName AS doctorName,
            w.wardName AS wardName,
            r.roomName AS roomName,
            b.bedNumber AS bedName,
            cl.careLevelName AS careLevelName,

            i.conditionNotes AS conditionNotes,
            i.initialDiagnosis AS initialDiagnosis,
            i.icd AS icdName,
            pc.patientConditionName AS patientConditionName,
            i.admissionPriority AS admissionPriority

        FROM Inpatient i
        JOIN i.patient p
        LEFT JOIN p.patientGender g
        LEFT JOIN i.admissionCategory ac
        LEFT JOIN i.admissionType at
        LEFT JOIN i.admissionSource asrc
        LEFT JOIN i.admissionStatus ast
        LEFT JOIN i.admittingWardId w
        LEFT JOIN i.room r
        LEFT JOIN i.bed b
        LEFT JOIN i.careLevel cl
        LEFT JOIN i.icdDiagnosis icd
        LEFT JOIN i.patientCondition pc
        WHERE i.inpatientId = :inpatientId
        """)
        Optional<InpatientAdmissionProjection> findAdmissionDetailsByInpatientId(@Param("inpatientId") Long inpatientId);


        @Query(value = """
        SELECT
            i.inpatient_id AS inpatientId,
          CONCAT_WS(' ',   p.p_fn,  p.p_mn,p.p_ln) AS patientName,
            p.uhid_no AS uhid,
            p.p_age AS age,
           g.id AS genderId,
           g.gender_name AS gender,
            p.p_mobile_number AS mobileNo,
            p.emer_mobile AS emergencyMobileNo,
            i.admission_no AS admissionNo,
            w.ward_id AS wardId,
            w.ward_name AS ward,
            r.room_id AS rooId,
            r.room_name AS room,
            b.bed_id AS bedId,
            b.bed_number AS bed,
          CAST(i.admission_date AS timestamp) + i.admission_time AS admissionDateTime,
           CASE
       WHEN i.discharge_date IS NOT NULL
           AND i.discharge_time IS NOT NULL
        THEN CAST(i.discharge_date AS timestamp)
           + i.discharge_time
         ELSE NULL
         END AS dischargeDate,
            wc.ward_category_id AS categoryId,
            wc.ward_category_name AS categoryName,
            i.doctor_name AS doctorName,
            CAST(
                CURRENT_DATE - i.admission_date
                AS varchar
            ) AS los,
            s.status_code AS status,
            bt.billing_type_name AS billingType

        FROM public.inpatient i

        INNER JOIN public.patient p
            ON p.patient_id = i.patient
        LEFT JOIN public.mas_gender g
            ON g.id = p.p_gender_id
        LEFT JOIN public.mas_ward w
            ON w.ward_id = i.admitting_ward_id
        LEFT JOIN public.mas_room r
            ON r.room_id = i.room_id
        LEFT JOIN public.mas_bed b
            ON b.bed_id = i.bed_id
        LEFT JOIN public.mas_ward_category wc
            ON wc.ward_category_id = i.ward_category_id
        LEFT JOIN public.mas_admission_status s
            ON s.admission_status_id = i.admission_status
        LEFT JOIN public.ipd_billing_header ibh
            ON ibh.inpatient_id = i.inpatient_id
        LEFT JOIN public.mas_ipd_billing_type bt
            ON bt.billing_type_id = ibh.billing_type_id

        WHERE
            i.admission_status = :admissionStatus
            AND (
                :patientName IS NULL
                OR :patientName = ''
                OR LOWER(
                    CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
            )
            AND (
                :mobileNo IS NULL
                OR :mobileNo = ''
                OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
            )
            AND (
                :admissionNo IS NULL
                OR :admissionNo = ''
                OR LOWER(i.admission_no)
                    LIKE LOWER(CONCAT('%', :admissionNo, '%'))
            )
            AND (
                :wardId IS NULL
                OR i.admitting_ward_id = :wardId
            )
        ORDER BY i.inpatient_id DESC

        """,

                countQuery = """
        SELECT COUNT(i.inpatient_id)
        FROM public.inpatient i
        INNER JOIN public.patient p
            ON p.patient_id = i.patient
        WHERE
            i.admission_status = :admissionStatus
            AND (
                :patientName IS NULL
                OR :patientName = ''
                OR LOWER(
                    CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
            )
            AND (
                :mobileNo IS NULL
                OR :mobileNo = ''
                OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
            )
            AND (
                :admissionNo IS NULL
                OR :admissionNo = ''
                OR LOWER(i.admission_no)
                    LIKE LOWER(CONCAT('%', :admissionNo, '%'))
            )
            AND (
                :wardId IS NULL
                OR i.admitting_ward_id = :wardId
            )
        """,
                nativeQuery = true)
        Page<ActiveAdmissionProjectionResponse> findActiveAdmissions(
                @Param("admissionStatus") Integer admissionStatus,
                @Param("patientName") String patientName,
                @Param("mobileNo") String mobileNo,
                @Param("admissionNo") String admissionNo,
                @Param("wardId") Long wardId,
                Pageable pageable
        );
    @Query(value = """
    SELECT
        i.inpatient_id AS inpatientId,
        CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln) AS patientName,
        p.uhid_no AS uhid,
        p.p_age AS age,
        g.id AS genderId,
        g.gender_name AS gender,
        p.p_mobile_number AS mobileNo,
        i.admission_no AS admissionNo,
        w.ward_id AS wardId,
        w.ward_name AS ward,
        r.room_id AS rooId,
        r.room_name AS room,
        b.bed_id AS bedId,
        b.bed_number AS bed,
        CAST(i.admission_date AS timestamp) + i.admission_time
            AS admissionDateTime,
        i.doctor_name AS doctorName,
        CASE
            WHEN ido.status =:status THEN ido.status
            ELSE NULL
        END AS dietStatus,
        dt.diet_type_id AS dietTypeId,
        dt.diet_type_name AS dietTypeName,
        ido.special_instruction AS specialInstructio
    FROM public.inpatient i
    INNER JOIN public.patient p
        ON p.patient_id = i.patient
    LEFT JOIN public.mas_gender g
        ON g.id = p.p_gender_id
    LEFT JOIN public.mas_ward w
        ON w.ward_id = i.admitting_ward_id
    LEFT JOIN public.mas_room r
        ON r.room_id = i.room_id
    LEFT JOIN public.mas_bed b
        ON b.bed_id = i.bed_id
    LEFT JOIN public.mas_admission_status s
        ON s.admission_status_id = i.admission_status
    LEFT JOIN public.ip_diet_order ido
        ON ido.inpatient_id = i.inpatient_id
        AND ido.status =:status
    LEFT JOIN public.mas_diet_type dt
        ON dt.diet_type_id = ido.diet_type_id
    WHERE
        i.admission_status =:admitAdmissionStatusId
        AND (
            :patientName IS NULL
            OR :patientName = ''
            OR LOWER(
                CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)
            ) LIKE LOWER(CONCAT('%', :patientName, '%'))
        )
        AND (
            :mobileNo IS NULL
            OR :mobileNo = ''
            OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
        )
        AND (
            :wardId IS NULL
            OR i.admitting_ward_id = :wardId
        )
    ORDER BY i.inpatient_id DESC
    """,
            countQuery = """
    SELECT COUNT(DISTINCT i.inpatient_id)
    FROM public.inpatient i
    INNER JOIN public.patient p
        ON p.patient_id = i.patient
    WHERE
        i.admission_status =:admitAdmissionStatusId
        AND (
            :patientName IS NULL
            OR :patientName = ''
            OR LOWER(
                CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)
            ) LIKE LOWER(CONCAT('%', :patientName, '%'))
        )
        AND (
            :mobileNo IS NULL
            OR :mobileNo = ''
            OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
        )
        AND (
            :wardId IS NULL
            OR i.admitting_ward_id = :wardId
        )
    """, nativeQuery = true)
    Page<InpatientDietOrderProjection> activeDietByInpatient(
            @Param("status") String status,
            @Param("admitAdmissionStatusId") Long admitAdmissionStatusId,
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("wardId") Long wardId,
            Pageable pageable);


    @Query(value = """
        SELECT
            i.inpatient_id AS inpatientId,
            i.visit_id AS visitId,
            p.patient_id AS patientId,

            CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln) AS patientName,

            p.uhid_no AS uhid,
            p.p_age AS age,

            g.id AS genderId,
            g.gender_name AS gender,

            p.p_mobile_number AS mobileNo,
            p.emer_mobile AS emergencyMobileNo,

            i.admission_no AS admissionNo,

            w.ward_id AS wardId,
            w.ward_name AS ward,

            r.room_id AS rooId,
            r.room_name AS room,

            b.bed_id AS bedId,
            b.bed_number AS bed,

            CAST(i.admission_date AS timestamp) + i.admission_time
                AS admissionDateTime,

            i.doctor_name AS doctorName

        FROM public.inpatient i

        INNER JOIN public.patient p
            ON p.patient_id = i.patient

        LEFT JOIN public.mas_gender g
            ON g.id = p.p_gender_id

        LEFT JOIN public.mas_ward w
            ON w.ward_id = i.admitting_ward_id

        LEFT JOIN public.mas_room r
            ON r.room_id = i.room_id

        LEFT JOIN public.mas_bed b
            ON b.bed_id = i.bed_id

        WHERE
            i.admission_status = :admissionStatus

            AND (
                :patientName IS NULL
                OR :patientName = ''
                OR LOWER(
                    CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
            )

            AND (
                :mobileNo IS NULL
                OR :mobileNo = ''
                OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
            )

            AND (
                :admissionNo IS NULL
                OR :admissionNo = ''
                OR LOWER(i.admission_no)
                    LIKE LOWER(CONCAT('%', :admissionNo, '%'))
            )

            AND (
                :wardId IS NULL
                OR i.admitting_ward_id = :wardId
            )

        ORDER BY i.inpatient_id DESC
        """,

            countQuery = """
        SELECT COUNT(i.inpatient_id)

        FROM public.inpatient i

        INNER JOIN public.patient p
            ON p.patient_id = i.patient

        WHERE
            i.admission_status = :admissionStatus

            AND (
                :patientName IS NULL
                OR :patientName = ''
                OR LOWER(
                    CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
            )

            AND (
                :mobileNo IS NULL
                OR :mobileNo = ''
                OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
            )

            AND (
                :admissionNo IS NULL
                OR :admissionNo = ''
                OR LOWER(i.admission_no)
                    LIKE LOWER(CONCAT('%', :admissionNo, '%'))
            )

            AND (
                :wardId IS NULL
                OR i.admitting_ward_id = :wardId
            )
        """,
            nativeQuery = true)
    Page<ActiveAdmissionOtProjection> activeAdmissionList(
            @Param("admissionStatus") Long admissionStatus,
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("admissionNo") String admissionNo,
            @Param("wardId") Long wardId,
            Pageable pageable
    );
}
