package com.hims.response;
import com.hims.entity.*; import lombok.*; import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtPacAssessmentResponse {
 private Long otBookingId;
 private OtPacAssessment assessment;
 private List<OtPacMedicalHistory> medicalHistory;
 private List<OtPacInstruction> instructions;
 private List<OtPacDocument> documents;
}
