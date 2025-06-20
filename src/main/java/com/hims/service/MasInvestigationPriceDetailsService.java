package com.hims.service;

import com.hims.request.MasInvestigationPriceDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInvestigationPriceDetailsResponse;

import java.util.List;

public interface MasInvestigationPriceDetailsService {

    ApiResponse<List<MasInvestigationPriceDetailsResponse>> getAllPriceDetails(int flag);

    ApiResponse<MasInvestigationPriceDetailsResponse> findById(Long id);

    public ApiResponse<List<MasInvestigationPriceDetailsResponse>> findByInvestigationId(Long investigationId);

    ApiResponse<MasInvestigationPriceDetailsResponse> addPriceDetails(MasInvestigationPriceDetailsRequest request);

    ApiResponse<MasInvestigationPriceDetailsResponse> updatePriceDetails(Long id, MasInvestigationPriceDetailsRequest request);

    ApiResponse<MasInvestigationPriceDetailsResponse> changeStatus(Long id, String status);
}