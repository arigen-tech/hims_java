package com.hims.service;

import com.hims.request.InvestigationValidationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.SampleValidationResponse;

import java.util.List;

public interface SampleValidationService {

    void validateInvestigations(List<InvestigationValidationRequest> requests);

    ApiResponse<List<SampleValidationResponse>>getInvestigationsWithOrderStatusNAndP();
}
