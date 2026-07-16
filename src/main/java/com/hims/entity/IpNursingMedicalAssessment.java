package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_nursing_medical_assessment")
public class IpNursingMedicalAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private MasHospital hospital;

    @Column(name = "consciousness", length = 100)
    private String consciousness;

    @Column(name = "gcs_score")
    private Integer gcsScore;

    @Column(name = "pain_score")
    private Integer painScore;

    @Column(name = "mobility_status", length = 100)
    private String mobilityStatus;

    @Column(name = "fall_risk", length = 50)
    private String fallRisk;

    @Column(name = "pressure_sore_risk", length = 50)
    private String pressureSoreRisk;

    @Column(name = "skin_condition", length = 100)
    private String skinCondition;

    @Column(name = "skin_remarks", length = 500)
    private String skinRemarks;

    @Column(name = "iv_line_present", length = 1)
    private String ivLinePresent;

    @Column(name = "iv_site", length = 100)
    private String ivSite;

    @Column(name = "catheter_present", length = 1)
    private String catheterPresent;

    @Column(name = "catheter_type", length = 100)
    private String catheterType;

    @Column(name = "drain_present", length = 1)
    private String drainPresent;

    @Column(name = "drain_type", length = 100)
    private String drainType;

    @Column(name = "nutrition_risk", length = 50)
    private String nutritionRisk;

    @Column(name = "nutrition_remarks", length = 500)
    private String nutritionRemarks;

    @Column(name = "infection_risk", length = 50)
    private String infectionRisk;

    @Column(name = "infection_remarks", length = 500)
    private String infectionRemarks;

    @Column(name = "patient_orientation_done", length = 1)
    private String patientOrientationDone;

    @Column(name = "relative_orientation_done", length = 1)
    private String relativeOrientationDone;

    @Column(name = "nursing_care_plan", columnDefinition = "text")
    private String nursingCarePlan;

    @Column(name = "chief_complaint", columnDefinition = "text")
    private String chiefComplaint;

    @Column(name = "history_present_illness", columnDefinition = "text")
    private String historyPresentIllness;

    @Column(name = "family_history", columnDefinition = "text")
    private String familyHistory;

    @Column(name = "medication_history", columnDefinition = "text")
    private String medicationHistory;

    @Column(name = "allergies", columnDefinition = "text")
    private String allergies;

    @Column(name = "pulse")
    private Integer pulse;

    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "temperature_unit", length = 10)
    private String temperatureUnit;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "spo2", precision = 5, scale = 2)
    private BigDecimal spo2;

    @Column(name = "general_examination_notes", columnDefinition = "text")
    private String generalExaminationNotes;

    @Column(name = "system_rs_examination", columnDefinition = "text")
    private String systemRsExamination;

    @Column(name = "system_cvs_examination", columnDefinition = "text")
    private String systemCvsExamination;

    @Column(name = "system_pa_examination", columnDefinition = "text")
    private String systemPaExamination;

    @Column(name = "system_cns_examination", columnDefinition = "text")
    private String systemCnsExamination;

    @Column(name = "provisional_diagnosis", columnDefinition = "text")
    private String provisionalDiagnosis;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 200)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}