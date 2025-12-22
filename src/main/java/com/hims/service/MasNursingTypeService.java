package com.hims.service;

import com.hims.request.MasNursingTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasNursingTypeResponse;

import java.util.List;

public interface MasNursingTypeService {
    ApiResponse<List<MasNursingTypeResponse>> getAll(int flag);

    ApiResponse<MasNursingTypeResponse> getById(Long id);

    ApiResponse<MasNursingTypeResponse> create(MasNursingTypeRequest request);

    ApiResponse<MasNursingTypeResponse> update(Long id, MasNursingTypeRequest request);

    ApiResponse<MasNursingTypeResponse> changeStatus(Long id, String status);
}
