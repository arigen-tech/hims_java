package com.hims.service;

import com.hims.request.ObMasPvLiquorRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasPvLiquorResponse;

import java.util.List;

public interface ObMasPvLiquorService {

    ApiResponse<List<ObMasPvLiquorResponse>> getAll(int flag);

    ApiResponse<ObMasPvLiquorResponse> getById(Long id);

    ApiResponse<ObMasPvLiquorResponse> create(
            ObMasPvLiquorRequest request);

    ApiResponse<ObMasPvLiquorResponse> update(
            Long id, ObMasPvLiquorRequest request);

    ApiResponse<ObMasPvLiquorResponse> changeStatus(
            Long id, String status);
}

