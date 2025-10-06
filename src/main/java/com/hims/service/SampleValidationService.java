package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.SampleValidationResponse;

public interface SampleValidationService {
    ApiResponse<SampleValidationResponse> getPatientInvestigations(Long patientId);
}
