package com.hims.entity.repository;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.projection.IPDPatientWaitingListProjection;
import com.hims.projection.PaidCancelledAppointmentProjection;
import com.hims.projection.PatientVitalsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OpdPatientDetailRepository extends JpaRepository<OpdPatientDetail, Long> {

    OpdPatientDetail findTopByPatientOrderByOpdPatientDetailsIdDesc(Patient patient);

    @Query(value = """
    SELECT 
        opd.height AS height,
        opd.weight AS weight,
        opd.temperature AS temperature,
        opd.bp_systolic AS bpSystolic,
        opd.bp_diastolic AS bpDiastolic,
        opd.pulse AS pulse,
        opd.rr AS rr,
        opd.spo2 AS spo2,
        opd.bmi AS bmi
    FROM opd_patient_details opd
    WHERE opd.patient_id = :patientId
    ORDER BY opd.opd_patient_details_id DESC
    LIMIT 1
""", nativeQuery = true)
    PatientVitalsProjection findLatestVitals(Long patientId);

    OpdPatientDetail findByVisit_Id(Long visitId);

    OpdPatientDetail findTopByVisit_IdOrderByOpdPatientDetailsIdDesc(Long visitId);

    OpdPatientDetail findByVisitId(Long visitId);
    @Query(
            value = """
        SELECT
            opd.opd_patient_details_id AS opdPatientDetailsId,
            opd.patient_id AS patientId,
            p.uhid_no AS uhid,
            opd.visit_id AS visitId,
            CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            ) AS patientName,
            p.p_mobile_number AS patientMobileNo,
            p.p_age AS age,
            mg.gender_name AS gender,
            opd.admission_advised_date AS admissionAdviseDate,
            CONCAT(
                COALESCE(u.first_name, ''), ' ',
                COALESCE(u.last_name, ''), ' ',
                COALESCE(u.middle_name, '')
            ) AS doctorName,
            md.department_id AS departmentId,
            md.department_name AS department,
            mds.department_id AS wardId,
            mds.department_name AS wardName,
            opd.admission_priority AS admissionType,
            mcl.care_id AS careLevelId,
            mcl.care_level_name AS careLevel,
            mwc.ward_category_id AS admissionWardCategoryId,
            mwc.ward_category_name AS admissionWardCategoryName

        FROM opd_patient_details opd
        LEFT JOIN patient p ON p.patient_id = opd.patient_id
        LEFT JOIN mas_gender mg ON mg.id = p.p_gender_id
        LEFT JOIN mas_employee emp ON emp.emp_id = opd.doctor_id
        LEFT JOIN mas_department md ON md.department_id = opd.department_id
        LEFT JOIN mas_department mds ON mds.department_id = opd.admission_ward_id
        LEFT JOIN mas_care_level mcl ON mcl.care_id = opd.admission_care_level_id
        LEFT JOIN mas_ward_category mwc ON mwc.ward_category_id = opd.admission_ward_category_id
        LEFT JOIN users u ON u.user_id = opd.doctor_id

        WHERE LOWER(opd.admission_flag) = :admissionFlag
          AND opd.hospital_id = :hospitalId

          AND (
                :patientName IS NULL
                OR LOWER(
                    CONCAT(
                        COALESCE(p.p_fn, ''), ' ',
                        COALESCE(p.p_mn, ''), ' ',
                        COALESCE(p.p_ln, '')
                    )
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
          )

          AND (
                :mobileNo IS NULL
                OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
          )

        ORDER BY opd.opd_patient_details_id DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM opd_patient_details opd
        LEFT JOIN patient p ON p.patient_id = opd.patient_id

        WHERE LOWER(opd.admission_flag) = :admissionFlag
          AND opd.hospital_id = :hospitalId

          AND (
                :patientName IS NULL
                OR LOWER(
                    CONCAT(
                        COALESCE(p.p_fn, ''), ' ',
                        COALESCE(p.p_mn, ''), ' ',
                        COALESCE(p.p_ln, '')
                    )
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
          )

          AND (
                :mobileNo IS NULL
                OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
          )
        """,
            nativeQuery = true
    )
    Page<IPDPatientWaitingListProjection> getIPDPatientWaitingList(
            @Param("admissionFlag") String admissionFlag,
            @Param("hospitalId") Long hospitalId,
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            Pageable pageable
    );
    public interface VisitRepository extends JpaRepository<Visit, Long> {

        @Query(
                value = """
            SELECT
                v.visit_id AS visitId,
                p.patient_id AS patientId,

                p.uhid_no AS registrationNo,

                TRIM(CONCAT(
                    COALESCE(p.p_fn, ''),
                    ' ',
                    COALESCE(p.p_mn, ''),
                    ' ',
                    COALESCE(p.p_ln, '')
                )) AS patientName,

                p.p_mobile_number AS mobileNo,
                p.p_age AS age,
                g.gender_name AS gender,

                v.department_id AS departmentId,
                d.department_name AS departmentName,

                v.doctor_id AS doctorId,
                v.doctor_name AS doctorName,

                v.visit_date AS appointmentDate,
                v.start_time AS appointmentTime,
                v.cancelled_date AS cancelledDate,

                bh.billing_hd_id AS billingHeaderId,
                bh.bill_no AS billNo,
                bh.bill_date AS billDate,

                CASE
                    WHEN bh.service_category_code = 'SC001'
                        THEN 'OPD'
                    WHEN bh.service_category_code = 'SC002'
                        THEN 'LABORATORY'
                    WHEN bh.service_category_code = 'SC004'
                        THEN 'RADIOLOGY'
                    ELSE bh.service_category_code
                END AS billingService,

                bh.net_amount AS billingAmount,

                COALESCE(bh.patient_refund_amount, 0)
                    AS refundAmount,

                CASE
                    WHEN LOWER(COALESCE(bh.refund_status, 'n'))
                         IN ('y', 'completed', 'refunded')
                    THEN 'COMPLETED'
                    ELSE 'PENDING'
                END AS refundStatus,

                bh.refund_date AS refundDate

            FROM visit v

            INNER JOIN patient p
                ON p.patient_id = v.patient_id

            LEFT JOIN mas_gender g
                ON g.gender_id = p.gender_id

            LEFT JOIN mas_department d
                ON d.department_id = v.department_id

            INNER JOIN billing_header bh
                ON bh.billing_hd_id = v.billing_hd_id

            WHERE LOWER(v.visit_status) = 'cancelled'

              AND LOWER(COALESCE(v.billing_status, ''))
                  IN ('y', 'paid', 'completed')

              AND COALESCE(bh.net_amount, 0) > 0

              AND (
                    :patientName IS NULL
                    OR :patientName = ''
                    OR LOWER(
                        CONCAT(
                            COALESCE(p.p_fn, ''),
                            ' ',
                            COALESCE(p.p_mn, ''),
                            ' ',
                            COALESCE(p.p_ln, '')
                        )
                    ) LIKE LOWER(
                        CONCAT('%', :patientName, '%')
                    )
              )

              AND (
                    :mobileNo IS NULL
                    OR :mobileNo = ''
                    OR p.p_mobile_number
                        LIKE CONCAT('%', :mobileNo, '%')
              )

              AND (
                    :billingService IS NULL
                    OR :billingService = ''
                    OR (
                        :billingService = 'OPD'
                        AND bh.service_category_code = 'SC001'
                    )
                    OR (
                        :billingService = 'LABORATORY'
                        AND bh.service_category_code = 'SC002'
                    )
                    OR (
                        :billingService = 'RADIOLOGY'
                        AND bh.service_category_code = 'SC004'
                    )
              )

              AND (
                    (
                        :refundStatus = 'PENDING'

                        AND LOWER(
                            COALESCE(bh.refund_status, 'n')
                        ) NOT IN (
                            'y',
                            'completed',
                            'refunded'
                        )

                        AND CAST(v.cancelled_date AS DATE)
                            BETWEEN :fromDate AND :toDate
                    )

                    OR

                    (
                        :refundStatus = 'COMPLETED'

                        AND LOWER(
                            COALESCE(bh.refund_status, 'n')
                        ) IN (
                            'y',
                            'completed',
                            'refunded'
                        )

                        AND CAST(bh.refund_date AS DATE)
                            BETWEEN :fromDate AND :toDate
                    )

                    OR

                    (
                        :refundStatus = 'ALL'

                        AND (
                            (
                                LOWER(
                                    COALESCE(
                                        bh.refund_status,
                                        'n'
                                    )
                                ) NOT IN (
                                    'y',
                                    'completed',
                                    'refunded'
                                )

                                AND CAST(
                                    v.cancelled_date AS DATE
                                ) BETWEEN :fromDate AND :toDate
                            )

                            OR

                            (
                                LOWER(
                                    COALESCE(
                                        bh.refund_status,
                                        'n'
                                    )
                                ) IN (
                                    'y',
                                    'completed',
                                    'refunded'
                                )

                                AND CAST(
                                    bh.refund_date AS DATE
                                ) BETWEEN :fromDate AND :toDate
                            )
                        )
                    )
              )

            ORDER BY
                COALESCE(
                    bh.refund_date,
                    v.cancelled_date
                ) DESC
            """,

                countQuery = """
            SELECT COUNT(v.visit_id)

            FROM visit v

            INNER JOIN patient p
                ON p.patient_id = v.patient_id

            INNER JOIN billing_header bh
                ON bh.billing_hd_id = v.billing_hd_id

            WHERE LOWER(v.visit_status) = 'cancelled'

              AND LOWER(COALESCE(v.billing_status, ''))
                  IN ('y', 'paid', 'completed')

              AND COALESCE(bh.net_amount, 0) > 0

              AND (
                    :patientName IS NULL
                    OR :patientName = ''
                    OR LOWER(
                        CONCAT(
                            COALESCE(p.p_fn, ''),
                            ' ',
                            COALESCE(p.p_mn, ''),
                            ' ',
                            COALESCE(p.p_ln, '')
                        )
                    ) LIKE LOWER(
                        CONCAT('%', :patientName, '%')
                    )
              )

              AND (
                    :mobileNo IS NULL
                    OR :mobileNo = ''
                    OR p.p_mobile_number
                        LIKE CONCAT('%', :mobileNo, '%')
              )

              AND (
                    :billingService IS NULL
                    OR :billingService = ''
                    OR (
                        :billingService = 'OPD'
                        AND bh.service_category_code = 'SC001'
                    )
                    OR (
                        :billingService = 'LABORATORY'
                        AND bh.service_category_code = 'SC002'
                    )
                    OR (
                        :billingService = 'RADIOLOGY'
                        AND bh.service_category_code = 'SC004'
                    )
              )

              AND (
                    (
                        :refundStatus = 'PENDING'

                        AND LOWER(
                            COALESCE(bh.refund_status, 'n')
                        ) NOT IN (
                            'y',
                            'completed',
                            'refunded'
                        )

                        AND CAST(v.cancelled_date AS DATE)
                            BETWEEN :fromDate AND :toDate
                    )

                    OR

                    (
                        :refundStatus = 'COMPLETED'

                        AND LOWER(
                            COALESCE(bh.refund_status, 'n')
                        ) IN (
                            'y',
                            'completed',
                            'refunded'
                        )

                        AND CAST(bh.refund_date AS DATE)
                            BETWEEN :fromDate AND :toDate
                    )

                    OR

                    (
                        :refundStatus = 'ALL'

                        AND (
                            (
                                LOWER(
                                    COALESCE(
                                        bh.refund_status,
                                        'n'
                                    )
                                ) NOT IN (
                                    'y',
                                    'completed',
                                    'refunded'
                                )

                                AND CAST(
                                    v.cancelled_date AS DATE
                                ) BETWEEN :fromDate AND :toDate
                            )

                            OR

                            (
                                LOWER(
                                    COALESCE(
                                        bh.refund_status,
                                        'n'
                                    )
                                ) IN (
                                    'y',
                                    'completed',
                                    'refunded'
                                )

                                AND CAST(
                                    bh.refund_date AS DATE
                                ) BETWEEN :fromDate AND :toDate
                            )
                        )
                    )
              )
            """,
                nativeQuery = true
        )
        Page<PaidCancelledAppointmentProjection> getBillingRefundPatientList(

                @Param("patientName")
                String patientName,

                @Param("mobileNo")
                String mobileNo,

                @Param("billingService")
                String billingService,

                @Param("fromDate")
                LocalDate fromDate,

                @Param("toDate")
                LocalDate toDate,

                @Param("refundStatus")
                String refundStatus,

                Pageable pageable
        );
    }
}
