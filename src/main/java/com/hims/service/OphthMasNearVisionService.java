package com.hims.service;

import com.hims.request.OphthMasNearVisionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OphthMasNearVisionResponse;

import java.util.List;

public interface OphthMasNearVisionService {
    ApiResponse<OphthMasNearVisionResponse> create(
            OphthMasNearVisionRequest request);

    ApiResponse<OphthMasNearVisionResponse> update(
            Long id, OphthMasNearVisionRequest request);

    ApiResponse<OphthMasNearVisionResponse> getById(Long id);

    ApiResponse<List<OphthMasNearVisionResponse>> getAll(int flag);

    ApiResponse<OphthMasNearVisionResponse> changeStatus(
            Long id, String status);
}
