package com.hims.service;

import com.hims.request.MasComponentFailureReasonRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasComponentFailureReasonResponse;

import java.util.List;

public interface MasComponentFailureReasonService {

    ApiResponse<List<MasComponentFailureReasonResponse>> getAll(int flag);

    ApiResponse<MasComponentFailureReasonResponse> getById(Long id);

    ApiResponse<MasComponentFailureReasonResponse> create(MasComponentFailureReasonRequest request);

    ApiResponse<MasComponentFailureReasonResponse> update(Long id, MasComponentFailureReasonRequest request);

    ApiResponse<MasComponentFailureReasonResponse> changeStatus(Long id, String status);
}