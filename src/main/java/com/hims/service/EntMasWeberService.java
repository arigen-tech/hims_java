package com.hims.service;

import com.hims.request.EntMasWeberRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasWeberResponse;

import java.util.List;

public interface EntMasWeberService {

    ApiResponse<List<EntMasWeberResponse>> getAll(int flag);

    ApiResponse<EntMasWeberResponse> getById(Long id);

    ApiResponse<EntMasWeberResponse> create(EntMasWeberRequest request);

    ApiResponse<EntMasWeberResponse> update(Long id, EntMasWeberRequest request);

    ApiResponse<EntMasWeberResponse> changeStatus(Long id, String status);
}
