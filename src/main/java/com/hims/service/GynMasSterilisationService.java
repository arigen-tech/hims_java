package com.hims.service;

import com.hims.request.GynMasSterilisationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasSterilisationResponse;

import java.util.List;

public interface GynMasSterilisationService {

    ApiResponse<List<GynMasSterilisationResponse>> getAll(int flag);

    ApiResponse<GynMasSterilisationResponse> getById(Long id);

    ApiResponse<GynMasSterilisationResponse> create(
            GynMasSterilisationRequest request);

    ApiResponse<GynMasSterilisationResponse> update(
            Long id, GynMasSterilisationRequest request);

    ApiResponse<GynMasSterilisationResponse> changeStatus(
            Long id, String status);
}
