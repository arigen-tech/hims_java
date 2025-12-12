package com.hims.service;

import com.hims.request.MasAdmissionStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionStatusResponse;

import java.util.List;

public interface MasAdmissionStatusService {
    ApiResponse<List<MasAdmissionStatusResponse>> getAll(int flag);

    ApiResponse<MasAdmissionStatusResponse> getById(Long id);

    ApiResponse<MasAdmissionStatusResponse> create(MasAdmissionStatusRequest request);

    ApiResponse<MasAdmissionStatusResponse> update(Long id, MasAdmissionStatusRequest request);

    ApiResponse<MasAdmissionStatusResponse> changeStatus(Long id, String status);
}
