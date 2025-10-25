package com.hims.service;

import com.hims.request.DgMasInvestigationRequest;
import com.hims.request.DgMasInvestigationSingleReqest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DgMasInvestigationService {
    ApiResponse<List<DgMasInvestigationResponse>> getPriceDetails(String genderApplicable);

    ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(int flag);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgMasInvestigationResponse> createInvestigation(DgMasInvestigationSingleReqest investigationRequest);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgMasInvestigationResponse> updateSingleInvestigation(Long investigationId, DgMasInvestigationRequest investigationRequest);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse <DgMasInvestigationResponse> updateMultipleInvestigation(Long investigationId, DgMasInvestigationRequest investigationRequest);

}
