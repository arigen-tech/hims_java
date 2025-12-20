package com.hims.service;

import com.hims.request.OpthMasSpectacleUseRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpthMasSpectacleUseResponse;

import java.util.List;

public interface OpthMasSpectacleUseService {

    ApiResponse<List<OpthMasSpectacleUseResponse>> getAll(int flag);

    ApiResponse<OpthMasSpectacleUseResponse> getById(Long id);

    ApiResponse<OpthMasSpectacleUseResponse> create(
            OpthMasSpectacleUseRequest request);

    ApiResponse<OpthMasSpectacleUseResponse> update(
            Long id, OpthMasSpectacleUseRequest request);

    ApiResponse<OpthMasSpectacleUseResponse> changeStatus(
            Long id, String status);
}
