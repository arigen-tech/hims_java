package com.hims.service;

import com.hims.request.ObMasConsanguinityRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasConsanguinityResponse;

import java.util.List;

public interface ObMasConsanguinityService {

    ApiResponse<List<ObMasConsanguinityResponse>> getAll(int flag);

    ApiResponse<ObMasConsanguinityResponse> getById(Long id);

    ApiResponse<ObMasConsanguinityResponse> create(
            ObMasConsanguinityRequest request);

    ApiResponse<ObMasConsanguinityResponse> update(
            Long id, ObMasConsanguinityRequest request);

    ApiResponse<ObMasConsanguinityResponse> changeStatus(
            Long id, String status);
}
