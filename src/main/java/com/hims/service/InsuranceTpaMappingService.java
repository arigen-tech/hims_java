package com.hims.service;

import com.hims.request.InsuranceTpaMappingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.InsuranceTpaMappingResponse;

import java.util.List;

public interface InsuranceTpaMappingService {
    ApiResponse<List<InsuranceTpaMappingResponse>>
    getAllInsuranceTpaMapping(int flag);

    ApiResponse<InsuranceTpaMappingResponse>
    getByIdInsuranceTpaMapping(Long id);

    ApiResponse<InsuranceTpaMappingResponse>
    createInsuranceTpaMapping(InsuranceTpaMappingRequest request);

    ApiResponse<InsuranceTpaMappingResponse>
    updateInsuranceTpaMapping(Long id,
                              InsuranceTpaMappingRequest request);

    ApiResponse<InsuranceTpaMappingResponse>
    changeStatusInsuranceTpaMapping(Long id,
                                    String status);
}
