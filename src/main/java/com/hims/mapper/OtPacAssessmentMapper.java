package com.hims.mapper;
import com.hims.entity.*; import com.hims.request.OtPacAssessmentRequest; import com.hims.response.OtPacAssessmentResponse;
import org.springframework.beans.BeanUtils; import java.util.List;
public final class OtPacAssessmentMapper {
 private OtPacAssessmentMapper() {}
 public static void copyRequest(OtPacAssessmentRequest request, OtPacAssessment entity) {
  // clearanceStatus is deliberately excluded: the service maps UI values (C/P/N)
  // to the database constraint values (CLEARED/PENDING/NOT_CLEARED).
  BeanUtils.copyProperties(request, entity,
          "otBookingId", "submissionStatus", "clearanceStatus", "medicalHistory", "instructions");
 }
 public static OtPacAssessmentResponse toResponse(OtPacAssessment a, List<OtPacMedicalHistory> h, List<OtPacInstruction> i, List<OtPacDocument> d) {
  return OtPacAssessmentResponse.builder().otBookingId(a.getOtBooking().getOtBookingId()).assessment(a).medicalHistory(h).instructions(i).documents(d).build();
 }
}
