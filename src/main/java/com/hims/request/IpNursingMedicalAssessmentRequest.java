package com.hims.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IpNursingMedicalAssessmentRequest {

    @NotNull(message = "Inpatient ID is required")
    private Long inpatientId;

    @NotNull(message = "Hospital ID is required")
    private Long hospitalId;

    private String consciousness;

    @Min(value = 3, message = "GCS score must be at least 3")
    @Max(value = 15, message = "GCS score must not exceed 15")
    private Integer gcsScore;

    @Min(value = 0, message = "Pain score must be at least 0")
    @Max(value = 10, message = "Pain score must not exceed 10")
    private Integer painScore;

    @Size(max = 100, message = "Mobility status must not exceed 100 characters")
    private String mobilityStatus;

    @Size(max = 50, message = "Fall risk must not exceed 50 characters")
    private String fallRisk;

    @Size(max = 50, message = "Pressure sore risk must not exceed 50 characters")
    private String pressureSoreRisk;

    @Size(max = 100, message = "Skin condition must not exceed 100 characters")
    private String skinCondition;

    @Size(max = 500, message = "Skin remarks must not exceed 500 characters")
    private String skinRemarks;

    @Size(max = 1, message = "IV line present must contain only one character")
    private String ivLinePresent;

    @Size(max = 100, message = "IV site must not exceed 100 characters")
    private String ivSite;

    @Size(max = 1, message = "Catheter present must contain only one character")
    private String catheterPresent;

    @Size(max = 100, message = "Catheter type must not exceed 100 characters")
    private String catheterType;

    @Size(max = 1, message = "Drain present must contain only one character")
    private String drainPresent;

    @Size(max = 100, message = "Drain type must not exceed 100 characters")
    private String drainType;

    @Size(max = 50, message = "Nutrition risk must not exceed 50 characters")
    private String nutritionRisk;

    @Size(max = 500, message = "Nutrition remarks must not exceed 500 characters")
    private String nutritionRemarks;

    @Size(max = 50, message = "Infection risk must not exceed 50 characters")
    private String infectionRisk;

    @Size(max = 500, message = "Infection remarks must not exceed 500 characters")
    private String infectionRemarks;

    @Size(max = 1, message = "Patient orientation done must contain only one character")
    private String patientOrientationDone;

    @Size(max = 1, message = "Relative orientation done must contain only one character")
    private String relativeOrientationDone;

    private String nursingCarePlan;

    private String chiefComplaint;

    private String historyPresentIllness;

    private String familyHistory;

    private String medicationHistory;

    private String allergies;

    @Min(value = 0, message = "Pulse cannot be negative")
    private Integer pulse;

    @Min(value = 0, message = "Systolic BP cannot be negative")
    private Integer systolicBp;

    @Min(value = 0, message = "Diastolic BP cannot be negative")
    private Integer diastolicBp;

    private BigDecimal temperature;

//    @Size(max = 10, message = "Temperature unit must not exceed 10 characters")
//    private String temperatureUnit;

    @Min(value = 0, message = "Respiratory rate cannot be negative")
    private Integer respiratoryRate;

    @DecimalMin(value = "0.00", message = "SpO2 cannot be negative")
    @DecimalMax(value = "100.00", message = "SpO2 must not exceed 100")
    private BigDecimal spo2;

    private String generalExaminationNotes;

    private String rsExamination;

    private String cvsExamination;

    private String paExamination;

    private String cnsExamination;

    private String provisionalDiagnosis;


}