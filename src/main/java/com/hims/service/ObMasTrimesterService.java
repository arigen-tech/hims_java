package com.hims.service;

import com.hims.request.ObMasTrimesterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasTrimesterResponse;

import java.util.List;

public interface ObMasTrimesterService {

    ApiResponse<List<ObMasTrimesterResponse>> getAll(int flag);

    ApiResponse<ObMasTrimesterResponse> getById(Long id);

    ApiResponse<ObMasTrimesterResponse> create(
            ObMasTrimesterRequest request);

    ApiResponse<ObMasTrimesterResponse> update(
            Long id, ObMasTrimesterRequest request);

    ApiResponse<ObMasTrimesterResponse> changeStatus(
            Long id, String status);
}
