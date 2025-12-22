package com.hims.service;

import com.hims.request.ObMasBookedStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasBookedStatusResponse;

import java.util.List;

public interface ObMasBookedStatusService {

    ApiResponse<List<ObMasBookedStatusResponse>> getAll(int flag);

    ApiResponse<ObMasBookedStatusResponse> getById(Long id);

    ApiResponse<ObMasBookedStatusResponse> create(
            ObMasBookedStatusRequest request);

    ApiResponse<ObMasBookedStatusResponse> update(
            Long id, ObMasBookedStatusRequest request);

    ApiResponse<ObMasBookedStatusResponse> changeStatus(
            Long id, String status);
}
