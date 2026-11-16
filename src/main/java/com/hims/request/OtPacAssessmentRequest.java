
package com.hims.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtPacAssessmentRequest {

 public Long otBookingId;

 // D = Draft, S = Submit
 public String submissionStatus;

 public String currentMedications;
 public String allergyStatus;
 public String allergyDetails;
 public String previousAnaesthesia;
 public String previousAnaesthesiaDetails;

 public BigDecimal weightKg;
 public BigDecimal spo2;
 public BigDecimal temperature;
 public BigDecimal thyromentalDistanceCm;
 public BigDecimal mouthOpeningCm;

 public Short bpSystolic;
 public Short bpDiastolic;
 public Short pulse;
 public Short respiratoryRate;

 public String pallor;
 public String icterus;
 public String cyanosis;
 public String koilonychia;
 public String oedema;
 public String lymphadenopathy;

 public String mallampatiClass;
 public String tmjFindings;
 public String teethDentures;
 public String neckMovement;
 public String ivAccess;
 public String spineFindings;

 public String cvsFindings;
 public String rsFindings;
 public String cnsFindings;
 public String abdomenFindings;

 public String investigationReviewStatus;
 public String investigationFindings;

 public Long asaGradeId;
 public Long anaesthesiaTypeId;
 public Long specialistDepartmentId;

 public String asaEmergency;
 public String specialistReferralRequired;
 public String specialistReferralReason;

 public String clearanceStatus;
 public String pacRemarks;

 public List<MedicalHistory> medicalHistory;
 public List<String> instructions;

 @Getter
 @Setter
 @NoArgsConstructor
 @AllArgsConstructor
 @Builder
 public static class MedicalHistory {

  private String diseaseName;
  private String diseaseStatus;
  private String duration;
  private String remarks;
 }
}
