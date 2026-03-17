package com.hims.service;

import com.hims.request.MasInvestigationPriceDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInvestigationPriceDetailsProjectionResponse;
import com.hims.response.MasInvestigationPriceDetailsResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MasInvestigationPriceDetailsService {

    ApiResponse<Page<MasInvestigationPriceDetailsProjectionResponse>> getAllPriceDetails(
            int flag, int page, int size, String investigationName);


    ApiResponse<MasInvestigationPriceDetailsResponse> findById(Long id);

    public ApiResponse<List<MasInvestigationPriceDetailsResponse>> findByInvestigationId(Long investigationId);

    ApiResponse<MasInvestigationPriceDetailsResponse> addPriceDetails(MasInvestigationPriceDetailsRequest request);

    ApiResponse<MasInvestigationPriceDetailsResponse> updatePriceDetails(Long id, MasInvestigationPriceDetailsRequest request);

    ApiResponse<MasInvestigationPriceDetailsResponse> changeStatus(Long id, String status);
}