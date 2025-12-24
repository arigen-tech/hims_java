package com.hims.service;

import com.hims.request.EntMasEarCanalRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasEarCanalResponse;

import java.util.List;

public interface EntMasEarCanalService {

    ApiResponse<List<EntMasEarCanalResponse>> getAll(int flag);

    ApiResponse<EntMasEarCanalResponse> getById(Long id);

    ApiResponse<EntMasEarCanalResponse> create(EntMasEarCanalRequest request);

    ApiResponse<EntMasEarCanalResponse> update(Long id, EntMasEarCanalRequest request);

    ApiResponse<EntMasEarCanalResponse> changeStatus(Long id, String status);
}
