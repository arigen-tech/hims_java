package com.hims.service;

import com.hims.request.DgMasInvestigationMultiRequest;
import com.hims.request.DgMasInvestigationRequest;
import com.hims.request.DgMasInvestigationSingleReqest;
import com.hims.response.*;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface DgMasInvestigationService {
    ApiResponse<List<DgMasInvestigationPriceDetailsResponse>> getPriceDetails(String genderApplicable,Boolean radioFlag);

    ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(int flag,Long mainChargeCodeId);

    ApiResponse<String> changeInvestigationStatus(Long investigationId,String status);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgMasInvestigationSingleResponse> createInvestigation(DgMasInvestigationSingleReqest investigationRequest);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgMasInvestigationSingleResponse> updateSingleInvestigation(Long investigationId, DgMasInvestigationSingleReqest investigationRequest);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<String> updateMultipleInvestigation(DgMasInvestigationMultiRequest multiRequest);

    ApiResponse<Page<DgMasInvestigationResponse>> getAllInvestigationsDynamic(
            int flag, int page, int size, String search, Long mainChargeCodeId);


    List<Map<String, Object>> getInvestigationTypes();

    ApiResponse<List<DgMasInvestigationRes>> getAllInvestigations();

    ApiResponse<List<MasInvestigationByMainChargeCodeResponse>> dgMasInvestigationByMainChargeCodeId(Long mainChargeCodeId);

    ApiResponse<Page<InvestigationResponse>> getDgMasInvestigation(Long investigationId, String search, int page, int size);
}
