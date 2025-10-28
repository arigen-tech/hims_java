package com.hims.service;

import com.hims.request.DgMasInvestigationMultiRequest;
import com.hims.request.DgMasInvestigationRequest;
import com.hims.request.DgMasInvestigationSingleReqest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;
import com.hims.response.DgMasInvestigationSingleResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DgMasInvestigationService {
    ApiResponse<List<DgMasInvestigationResponse>> getPriceDetails(String genderApplicable);

    ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(int flag);

    ApiResponse<String> changeInvestigationStatus(Long investigationId,String status);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgMasInvestigationSingleResponse> createInvestigation(DgMasInvestigationSingleReqest investigationRequest);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgMasInvestigationSingleResponse> updateSingleInvestigation(Long investigationId, DgMasInvestigationSingleReqest investigationRequest);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<String> updateMultipleInvestigation(DgMasInvestigationMultiRequest multiRequest);

}
