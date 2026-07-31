package com.hims.service;

import com.hims.request.MasCrossMatchTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCrossMatchTypeResponse;

import java.util.List;

public interface MasCrossMatchTypeService {
    ApiResponse<List<MasCrossMatchTypeResponse>> getAll(int flag);

    ApiResponse<MasCrossMatchTypeResponse> getById(Long id);

    ApiResponse<MasCrossMatchTypeResponse> create(MasCrossMatchTypeRequest request);

    ApiResponse<MasCrossMatchTypeResponse> update(Long id, MasCrossMatchTypeRequest request);

    ApiResponse<MasCrossMatchTypeResponse> changeStatus(Long id, String status);
}
