package com.hims.service;

import com.hims.request.MasMaritalStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMaritalStatusResponse;

import java.util.List;

public interface MasMaritalStatusService {
    ApiResponse<MasMaritalStatusResponse> addMaritalStatus(MasMaritalStatusRequest request);
    ApiResponse<String> changeMaritalStatus(Long id, String status);
    ApiResponse<MasMaritalStatusResponse> editMaritalStatus(Long id, MasMaritalStatusRequest request);
    ApiResponse<MasMaritalStatusResponse> getMaritalStatusById(Long id);
    ApiResponse<List<MasMaritalStatusResponse>> getAllMaritalStatuses();
}
