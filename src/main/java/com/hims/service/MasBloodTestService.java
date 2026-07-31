package com.hims.service;

import com.hims.request.MasBloodTestRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodTestResponse;

import java.util.List;

public interface MasBloodTestService {

    ApiResponse<List<MasBloodTestResponse>> getAll(int flag);

    ApiResponse<MasBloodTestResponse> getById(Long id);

    ApiResponse<MasBloodTestResponse> create(MasBloodTestRequest request);

    ApiResponse<MasBloodTestResponse> update(Long id, MasBloodTestRequest request);

    ApiResponse<MasBloodTestResponse> changeStatus(Long id, String status);
}
