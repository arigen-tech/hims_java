package com.hims.service;

import com.hims.request.MasDietPreferenceRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDietPreferenceResponse;

import java.util.List;

public interface MasDietPreferenceService {
    ApiResponse<List<MasDietPreferenceResponse>> getAll(int flag);

    ApiResponse<MasDietPreferenceResponse> getById(Long id);

    ApiResponse<MasDietPreferenceResponse> create(MasDietPreferenceRequest request);

    ApiResponse<MasDietPreferenceResponse> update(Long id, MasDietPreferenceRequest request);

    ApiResponse<MasDietPreferenceResponse> changeStatus(Long id, String status);
}
