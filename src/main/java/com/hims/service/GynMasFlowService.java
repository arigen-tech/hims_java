package com.hims.service;

import com.hims.request.GynMasFlowRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasFlowResponse;

import java.util.List;

public interface GynMasFlowService {

    ApiResponse<List<GynMasFlowResponse>> getAll(int flag);

    ApiResponse<GynMasFlowResponse> getById(Long id);

    ApiResponse<GynMasFlowResponse> create(
            GynMasFlowRequest request);

    ApiResponse<GynMasFlowResponse> update(
            Long id, GynMasFlowRequest request);

    ApiResponse<GynMasFlowResponse> changeStatus(
            Long id, String status);
}
