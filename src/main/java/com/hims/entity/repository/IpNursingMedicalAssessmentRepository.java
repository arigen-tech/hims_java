package com.hims.entity.repository;

import com.hims.entity.IpNursingMedicalAssessment;
import com.hims.projection.IpNursingMedicalAssessmentProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IpNursingMedicalAssessmentRepository extends JpaRepository<IpNursingMedicalAssessment,Long> {

    @Query(value = """
            SELECT
                a.assessment_id AS assessmentId,
                a.inpatient_id AS inpatientId,
                a.hospital_id AS hospitalId,
                h.hospital_name AS hospitalName,
                a.consciousness AS consciousness,
                a.gcs_score AS gcsScore,
                a.pain_score AS painScore,
                a.mobility_status AS mobilityStatus,
                a.fall_risk AS fallRisk,
                a.pressure_sore_risk AS pressureSoreRisk,
                a.skin_condition AS skinCondition,
                a.skin_remarks AS skinRemarks,
                a.iv_line_present AS ivLinePresent,
                a.iv_site AS ivSite,
                a.catheter_present AS catheterPresent,
                a.catheter_type AS catheterType,
                a.drain_present AS drainPresent,
                a.drain_type AS drainType,
                a.nutrition_risk AS nutritionRisk,
                a.nutrition_remarks AS nutritionRemarks,
                a.infection_risk AS infectionRisk,
                a.infection_remarks AS infectionRemarks,
                a.patient_orientation_done AS patientOrientationDone,
                a.relative_orientation_done AS relativeOrientationDone,
                a.nursing_care_plan AS nursingCarePlan,
                a.chief_complaint AS chiefComplaint,
                a.history_present_illness AS historyPresentIllness,
                a.family_history AS familyHistory,
                a.medication_history AS medicationHistory,
                a.allergies AS allergies,
                a.pulse AS pulse,
                a.systolic_bp AS systolicBp,
                a.diastolic_bp AS diastolicBp,
                a.temperature AS temperature,
                a.temperature_unit AS temperatureUnit,
                a.respiratory_rate AS respiratoryRate,
                a.spo2 AS spo2,
                a.general_examination_notes AS generalExaminationNotes,
                a.system_rs_examination AS rsExamination,
                a.system_cvs_examination AS cvsExamination,
                a.system_pa_examination AS paExamination,
                a.system_cns_examination AS cnsExamination,
                a.provisional_diagnosis AS provisionalDiagnosis,
                a.status AS status,
                a.created_by AS createdBy,
                a.created_date AS createdDate,
                a.updated_by AS updatedBy,
                a.updated_date AS updatedDate
            FROM ip_nursing_medical_assessment a
            LEFT JOIN mas_hospital h ON h.hospital_id = a.hospital_id
            WHERE a.inpatient_id = :inpatientId
            ORDER BY a.assessment_id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<IpNursingMedicalAssessmentProjection> getNursingMedicalAssessmentByInpatientId(@Param("inpatientId") Long inpatientId);
}
