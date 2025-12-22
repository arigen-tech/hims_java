package com.hims.service;

import com.hims.request.ObMasPresentationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasPresentationResponse;

import java.util.List;

public interface ObMasPresentationService {

    ApiResponse<List<ObMasPresentationResponse>> getAll(int flag);

    ApiResponse<ObMasPresentationResponse> getById(Long id);

    ApiResponse<ObMasPresentationResponse> create(
            ObMasPresentationRequest request);

    ApiResponse<ObMasPresentationResponse> update(
            Long id, ObMasPresentationRequest request);

    ApiResponse<ObMasPresentationResponse> changeStatus(
            Long id, String status);
}
