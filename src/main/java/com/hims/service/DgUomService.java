package com.hims.service;

import com.hims.request.DgUomRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgUomResponse;

import java.util.List;

public interface DgUomService {
    ApiResponse<DgUomResponse> addDgUom(DgUomRequest dgUomRequest);

    ApiResponse<DgUomResponse> getByIdDgUom(Long id);

    ApiResponse<List<DgUomResponse>> getAllDgUom(int flag);

    ApiResponse<DgUomResponse> updateByStatusDgUom(Long id, String status);

    ApiResponse<DgUomResponse> updateByIdDgUom(Long id, DgUomRequest dgUomRequest);
}
