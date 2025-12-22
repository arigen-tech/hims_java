package com.hims.service;

import com.hims.request.OpthMasLensTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpthMasLensTypeResponse;

import java.util.List;

public interface OpthMasLensTypeService {

    ApiResponse<List<OpthMasLensTypeResponse>> getAll(int flag);

    ApiResponse<OpthMasLensTypeResponse> getById(Long id);

    ApiResponse<OpthMasLensTypeResponse> create(OpthMasLensTypeRequest request);

    ApiResponse<OpthMasLensTypeResponse> update(
            Long id, OpthMasLensTypeRequest request);

    ApiResponse<OpthMasLensTypeResponse> changeStatus(
            Long id, String status);
}
