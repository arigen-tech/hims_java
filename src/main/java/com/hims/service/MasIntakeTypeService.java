package com.hims.service;

import com.hims.request.MasIntakeTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasIntakeTypeResponse;

import java.util.List;

public interface MasIntakeTypeService {
    ApiResponse<List<MasIntakeTypeResponse>> getAll(int flag);

    ApiResponse<MasIntakeTypeResponse> getById(Long id);

    ApiResponse<MasIntakeTypeResponse> create(MasIntakeTypeRequest request);

    ApiResponse<MasIntakeTypeResponse> update(Long id, MasIntakeTypeRequest request);

    ApiResponse<MasIntakeTypeResponse> changeStatus(Long id, String status);
}
