package com.hims.service;

import com.hims.request.PatientConsentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PatientConsentResponse;
import org.springframework.core.io.Resource;
import java.util.List;

public interface PatientConsentService {
    ApiResponse<PatientConsentResponse> upload(PatientConsentRequest request);
    ApiResponse<List<PatientConsentResponse>> getConsents(Long admissionId, Long otBookingId);
    PatientConsentFile getDocument(Long patientConsentId);

    record PatientConsentFile(Resource resource, String fileName, String contentType) {}
}
