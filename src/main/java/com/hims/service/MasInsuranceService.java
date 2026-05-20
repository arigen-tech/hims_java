package com.hims.service;

import com.hims.request.MasInsuranceRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInsuranceResponse;

import java.util.List;

public interface MasInsuranceService {
    ApiResponse<List<MasInsuranceResponse>> getAllMasInsurance(int flag);

    ApiResponse<MasInsuranceResponse> getByIdInsurance(Long id);

    ApiResponse<MasInsuranceResponse> createInsurance(MasInsuranceRequest request);

    ApiResponse<MasInsuranceResponse> updateInsurance(Long id, MasInsuranceRequest request);

    ApiResponse<MasInsuranceResponse> changeStatusInsurance(Long id, String status);
}
