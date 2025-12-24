package com.hims.service;

import com.hims.request.EntMasPinnaRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasPinnaResponse;

import java.util.List;

public interface EntMasPinnaService {

    ApiResponse<List<EntMasPinnaResponse>> getAll(int flag);

    ApiResponse<EntMasPinnaResponse> getById(Long id);

    ApiResponse<EntMasPinnaResponse> create(EntMasPinnaRequest request);

    ApiResponse<EntMasPinnaResponse> update(Long id, EntMasPinnaRequest request);

    ApiResponse<EntMasPinnaResponse> changeStatus(Long id, String status);
}
