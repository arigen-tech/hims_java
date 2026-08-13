package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class IpNursingMedicalAssessmentResponse {

    private Long assessmentId;
    private Long inpatientId;
    private Long hospitalId;
    private String hospitalName;
    private String consciousness;
    private Integer gcsScore;
    private Integer painScore;
    private String mobilityStatus;
    private String fallRisk;
    private String pressureSoreRisk;
    private String skinCondition;
    private String skinRemarks;
    private String ivLinePresent;
    private String ivSite;
    private String catheterPresent;
    private String catheterType;
    private String drainPresent;
    private String drainType;
    private String nutritionRisk;
    private String nutritionRemarks;
    private String infectionRisk;
    private String infectionRemarks;
    private String patientOrientationDone;
    private String relativeOrientationDone;
    private String nursingCarePlan;
    private String chiefComplaint;
    private String historyPresentIllness;
    private String familyHistory;
    private String medicationHistory;
    private String allergies;
    private Integer pulse;
    private Integer systolicBp;
    private Integer diastolicBp;
    private BigDecimal temperature;
    private String temperatureUnit;
    private Integer respiratoryRate;
    private BigDecimal spo2;
    private String generalExaminationNotes;
    private String rsExamination;
    private String cvsExamination;
    private String paExamination;
    private String cnsExamination;
    private String provisionalDiagnosis;
    private String status;
    private String createdBy;
    private LocalDateTime createdDate;
    private String updatedBy;
    private LocalDateTime updatedDate;
}
