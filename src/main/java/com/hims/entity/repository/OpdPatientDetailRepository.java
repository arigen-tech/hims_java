package com.hims.entity.repository;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.projection.PatientVitalsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    OpdPatientDetail findByVisitId(Long visitId);
}
