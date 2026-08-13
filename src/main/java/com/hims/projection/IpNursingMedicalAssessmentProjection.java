package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IpNursingMedicalAssessmentProjection {

    Long getAssessmentId();
    Long getInpatientId();
    Long getHospitalId();
    String getHospitalName();
    String getConsciousness();
    Integer getGcsScore();
    Integer getPainScore();
    String getMobilityStatus();
    String getFallRisk();
    String getPressureSoreRisk();
    String getSkinCondition();
    String getSkinRemarks();
    String getIvLinePresent();
    String getIvSite();
    String getCatheterPresent();
    String getCatheterType();
    String getDrainPresent();
    String getDrainType();
    String getNutritionRisk();
    String getNutritionRemarks();
    String getInfectionRisk();
    String getInfectionRemarks();
    String getPatientOrientationDone();
    String getRelativeOrientationDone();
    String getNursingCarePlan();
    String getChiefComplaint();
    String getHistoryPresentIllness();
    String getFamilyHistory();
    String getMedicationHistory();
    String getAllergies();
    Integer getPulse();
    Integer getSystolicBp();
    Integer getDiastolicBp();
    BigDecimal getTemperature();
    String getTemperatureUnit();
    Integer getRespiratoryRate();
    BigDecimal getSpo2();
    String getGeneralExaminationNotes();
    String getRsExamination();
    String getCvsExamination();
    String getPaExamination();
    String getCnsExamination();
    String getProvisionalDiagnosis();
    String getStatus();
    String getCreatedBy();
    LocalDateTime getCreatedDate();
    String getUpdatedBy();
    LocalDateTime getUpdatedDate();
}
