package com.hims.service;

import com.hims.request.MasProcedurePricingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedurePricingResponse;
import org.springframework.data.domain.Page;

public interface MasProcedurePricingService {
    ApiResponse<String> addMasProcedurePricing(MasProcedurePricingRequest request);

    ApiResponse<String> updateMasProcedurePricing(Long id, MasProcedurePricingRequest request);

    ApiResponse<String> changeStatusMasProcedurePricing(Long id, String status);

    ApiResponse<MasProcedurePricingResponse> getByIdMasProcedurePricing(Long id);

    ApiResponse<Page<MasProcedurePricingResponse>> getAllMasProcedurePricing(Long billingTypeId, String procedureName, int page, int size);
}
