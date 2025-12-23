package com.hims.service;

import com.hims.request.ObMasPelvisTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasPelvisTypeResponse;

import java.util.List;

public interface ObMasPelvisTypeService {

    ApiResponse<List<ObMasPelvisTypeResponse>> getAll(int flag);

    ApiResponse<ObMasPelvisTypeResponse> getById(Long id);

    ApiResponse<ObMasPelvisTypeResponse> create(
            ObMasPelvisTypeRequest request);

    ApiResponse<ObMasPelvisTypeResponse> update(
            Long id, ObMasPelvisTypeRequest request);

    ApiResponse<ObMasPelvisTypeResponse> changeStatus(
            Long id, String status);
}
