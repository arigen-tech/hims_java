package com.hims.service;

import com.hims.request.MasRouteRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRouteResponse;

import java.util.List;

public interface MasRouteService {
    ApiResponse<List<MasRouteResponse>> getAll(int flag);

    ApiResponse<MasRouteResponse> getById(Long id);

    ApiResponse<MasRouteResponse> create(MasRouteRequest request);

    ApiResponse<MasRouteResponse> update(Long id, MasRouteRequest request);

    ApiResponse<MasRouteResponse> changeStatus(Long id, String status);
}
