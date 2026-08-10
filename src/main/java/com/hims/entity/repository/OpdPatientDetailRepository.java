package com.hims.entity.repository;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.projection.IPDPatientWaitingListProjection;
import com.hims.projection.PatientVitalsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    PatientVitalsProjection findLatestVitals(@Param("patientId")Long patientId);

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





}
