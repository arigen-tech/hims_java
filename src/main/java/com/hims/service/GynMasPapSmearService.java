package com.hims.service;

import com.hims.request.GynMasPapSmearRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasPapSmearResponse;

import java.util.List;

public interface GynMasPapSmearService {

    ApiResponse<List<GynMasPapSmearResponse>> getAll(int flag);

    ApiResponse<GynMasPapSmearResponse> getById(Long id);

    ApiResponse<GynMasPapSmearResponse> create(
            GynMasPapSmearRequest request);

    ApiResponse<GynMasPapSmearResponse> update(
            Long id, GynMasPapSmearRequest request);

    ApiResponse<GynMasPapSmearResponse> changeStatus(
            Long id, String status);
}
