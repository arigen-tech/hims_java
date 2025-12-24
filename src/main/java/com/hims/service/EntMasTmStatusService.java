package com.hims.service;

import com.hims.request.EntMasTmStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasTmStatusResponse;

import java.util.List;

public interface EntMasTmStatusService {

    ApiResponse<List<EntMasTmStatusResponse>> getAll(int flag);

    ApiResponse<EntMasTmStatusResponse> getById(Long id);

    ApiResponse<EntMasTmStatusResponse> create(EntMasTmStatusRequest request);

    ApiResponse<EntMasTmStatusResponse> update(Long id, EntMasTmStatusRequest request);

    ApiResponse<EntMasTmStatusResponse> changeStatus(Long id, String status);
}
