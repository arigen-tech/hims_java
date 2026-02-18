package com.hims.service;

import com.hims.request.MasBloodCompatibilityRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodCompatibilityResponse;

import java.util.List;

public interface MasBloodCompatibilityService {

    ApiResponse<List<MasBloodCompatibilityResponse>> getAll(int flag);

    ApiResponse<MasBloodCompatibilityResponse> getById(Long id);

    ApiResponse<MasBloodCompatibilityResponse> create(
            MasBloodCompatibilityRequest request);

    ApiResponse<MasBloodCompatibilityResponse> update(
            Long id, MasBloodCompatibilityRequest request);

    ApiResponse<MasBloodCompatibilityResponse> changeStatus(
            Long id, String status);
}
