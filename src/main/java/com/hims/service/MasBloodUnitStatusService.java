package com.hims.service;

import com.hims.request.MasBloodUnitStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodUnitStatusResponse;

import java.util.List;

public interface MasBloodUnitStatusService {

    ApiResponse<List<MasBloodUnitStatusResponse>> getAll(int flag);

    ApiResponse<MasBloodUnitStatusResponse> getById(Long id);

    ApiResponse<MasBloodUnitStatusResponse> create(
            MasBloodUnitStatusRequest request);

    ApiResponse<MasBloodUnitStatusResponse> update(
            Long id, MasBloodUnitStatusRequest request);

    ApiResponse<MasBloodUnitStatusResponse> changeStatus(
            Long id, String status);
}
