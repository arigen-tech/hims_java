package com.hims.service;

import com.hims.request.ObMasCervixConsistencyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasCervixConsistencyResponse;

import java.util.List;

public interface ObMasCervixConsistencyService {

    ApiResponse<List<ObMasCervixConsistencyResponse>> getAll(int flag);

    ApiResponse<ObMasCervixConsistencyResponse> getById(Long id);

    ApiResponse<ObMasCervixConsistencyResponse> create(
            ObMasCervixConsistencyRequest request);

    ApiResponse<ObMasCervixConsistencyResponse> update(
            Long id, ObMasCervixConsistencyRequest request);

    ApiResponse<ObMasCervixConsistencyResponse> changeStatus(
            Long id, String status);
}
