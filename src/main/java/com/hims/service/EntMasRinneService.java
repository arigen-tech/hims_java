package com.hims.service;

import com.hims.request.EntMasRinneRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasRinneResponse;

import java.util.List;

public interface EntMasRinneService {

    ApiResponse<List<EntMasRinneResponse>> getAll(int flag);

    ApiResponse<EntMasRinneResponse> getById(Long id);

    ApiResponse<EntMasRinneResponse> create(EntMasRinneRequest request);

    ApiResponse<EntMasRinneResponse> update(Long id, EntMasRinneRequest request);

    ApiResponse<EntMasRinneResponse> changeStatus(Long id, String status);
}
