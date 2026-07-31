package com.hims.service;

import com.hims.request.MasSurgeryPricingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSurgeryPricingResponse;
import org.springframework.data.domain.Page;

public interface MasSurgeryPricingService {
    ApiResponse<String> addMasSurgeryPricing(MasSurgeryPricingRequest request);

    ApiResponse<String> updateMasSurgeryPricing(Long id, MasSurgeryPricingRequest request);

    ApiResponse<String> changeStatusMasSurgeryPricing(Long id, String status);

    ApiResponse<MasSurgeryPricingResponse> getByIdMasSurgeryPricing(Long id);

    ApiResponse<Page<MasSurgeryPricingResponse>> getAllMasSurgeryPricing(Long billingTypeId, String surgeryName, int page, int size);
}
