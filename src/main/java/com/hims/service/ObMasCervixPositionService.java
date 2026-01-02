package com.hims.service;

import com.hims.request.ObMasCervixPositionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasCervixPositionResponse;

import java.util.List;

public interface ObMasCervixPositionService {

    ApiResponse<List<ObMasCervixPositionResponse>> getAll(int flag);

    ApiResponse<ObMasCervixPositionResponse> getById(Long id);

    ApiResponse<ObMasCervixPositionResponse> create(
            ObMasCervixPositionRequest request);

    ApiResponse<ObMasCervixPositionResponse> update(
            Long id, ObMasCervixPositionRequest request);

    ApiResponse<ObMasCervixPositionResponse> changeStatus(
            Long id, String status);
}
