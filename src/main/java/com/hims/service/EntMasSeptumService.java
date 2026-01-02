package com.hims.service;

import com.hims.request.EntMasSeptumRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasSeptumResponse;

import java.util.List;

public interface EntMasSeptumService {

    ApiResponse<List<EntMasSeptumResponse>> getAll(int flag);

    ApiResponse<EntMasSeptumResponse> getById(Long id);

    ApiResponse<EntMasSeptumResponse> create(EntMasSeptumRequest request);

    ApiResponse<EntMasSeptumResponse> update(Long id, EntMasSeptumRequest request);

    ApiResponse<EntMasSeptumResponse> changeStatus(Long id, String status);
}
