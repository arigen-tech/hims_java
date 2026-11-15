package com.hims.service;
import com.hims.request.OtPacAssessmentRequest; import com.hims.response.*; import org.springframework.web.multipart.MultipartFile; import java.util.List;
public interface OtPacAssessmentService {
 ApiResponse<OtPacAssessmentResponse> save(OtPacAssessmentRequest request,List<MultipartFile> files,List<String> documentNames);
 ApiResponse<OtPacAssessmentResponse> getById(Long assessmentId);
 ApiResponse<OtPacAssessmentResponse> getByBookingId(Long bookingId);
}
