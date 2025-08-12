package com.hims.service;

import com.hims.request.DgMasInvestigationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DgMasInvestigationService {
    ApiResponse<List<DgMasInvestigationResponse>> getPriceDetails(String genderApplicable);

    ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(int flag);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgMasInvestigationResponse> createInvestigation(DgMasInvestigationRequest investigationRequest);
}
