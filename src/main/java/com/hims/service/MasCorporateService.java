package com.hims.service;

import com.hims.request.MasCorporateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCorporateResponse;
import lombok.Data;

import java.util.List;

public interface MasCorporateService {
    ApiResponse<List<MasCorporateResponse>> getAllMasCorporate(int flag);
    ApiResponse<MasCorporateResponse> getByIdCorporate(Long id);

    ApiResponse<MasCorporateResponse> createCorporate(MasCorporateRequest request);

    ApiResponse<MasCorporateResponse> updateCorporate(Long id, MasCorporateRequest request);

    ApiResponse<MasCorporateResponse> changeStatusCorporate(Long id, String status);
}
