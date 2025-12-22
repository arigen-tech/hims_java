package com.hims.service;

import com.hims.request.OpthMasDistanceVisionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpthMasDistanceVisionResponse;

import java.util.List;

public interface OpthMasDistanceVisionService {

    ApiResponse<List<OpthMasDistanceVisionResponse>> getAll(int flag);

    ApiResponse<OpthMasDistanceVisionResponse> getById(Long id);

    ApiResponse<OpthMasDistanceVisionResponse> create(
            OpthMasDistanceVisionRequest request);

    ApiResponse<OpthMasDistanceVisionResponse> update(
            Long id, OpthMasDistanceVisionRequest request);

    ApiResponse<OpthMasDistanceVisionResponse> changeStatus(
            Long id, String status);
}
