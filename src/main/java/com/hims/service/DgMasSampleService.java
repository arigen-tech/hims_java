package com.hims.service;

import com.hims.request.DgMasSampleRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasSampleResponse;

import java.util.List;

public interface DgMasSampleService {
    ApiResponse<DgMasSampleResponse> addDgMasSample(DgMasSampleRequest dgMasSampleRequest);

    ApiResponse<DgMasSampleResponse> getByIdDgMas(Long id);

    ApiResponse<List<DgMasSampleResponse>> getAllDgMas(int flag);

    ApiResponse<DgMasSampleResponse> updateByStatusDgMas(Long id, String status);

    ApiResponse<DgMasSampleResponse> updateByIdDgMas(Long id, DgMasSampleRequest dgMasSampleRequest);
}
