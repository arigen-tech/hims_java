package com.hims.service;

import com.hims.request.MasOutputTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOutputTypeResponse;

import java.util.List;

public interface MasOutputTypeService {
    ApiResponse<List<MasOutputTypeResponse>> getAll(int flag);

    ApiResponse<MasOutputTypeResponse> getById(Long id);

    ApiResponse<MasOutputTypeResponse> create(MasOutputTypeRequest request);

    ApiResponse<MasOutputTypeResponse> update(Long id, MasOutputTypeRequest request);

    ApiResponse<MasOutputTypeResponse> changeStatus(Long id, String status);
}

