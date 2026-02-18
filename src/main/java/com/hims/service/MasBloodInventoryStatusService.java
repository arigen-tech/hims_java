package com.hims.service;

import com.hims.request.MasBloodInventoryStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodInventoryStatusResponse;

import java.util.List;

public interface MasBloodInventoryStatusService {

    ApiResponse<List<MasBloodInventoryStatusResponse>> getAll(int flag);

    ApiResponse<MasBloodInventoryStatusResponse> getById(Long id);

    ApiResponse<MasBloodInventoryStatusResponse> create(
            MasBloodInventoryStatusRequest request);

    ApiResponse<MasBloodInventoryStatusResponse> update(
            Long id, MasBloodInventoryStatusRequest request);

    ApiResponse<MasBloodInventoryStatusResponse> changeStatus(
            Long id, String status);
}
