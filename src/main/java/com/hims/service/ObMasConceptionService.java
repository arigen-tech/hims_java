package com.hims.service;

import com.hims.request.ObMasConceptionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasConceptionResponse;

import java.util.List;

public interface ObMasConceptionService {

    ApiResponse<List<ObMasConceptionResponse>> getAll(int flag);

    ApiResponse<ObMasConceptionResponse> getById(Long id);

    ApiResponse<ObMasConceptionResponse> create(
            ObMasConceptionRequest request);

    ApiResponse<ObMasConceptionResponse> update(
            Long id, ObMasConceptionRequest request);

    ApiResponse<ObMasConceptionResponse> changeStatus(
            Long id, String status);
}
