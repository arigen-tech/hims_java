package com.hims.service;

import com.hims.request.MasDietTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDietTypeResponse;

import java.util.List;

public interface MasDietTypeService {
    ApiResponse<List<MasDietTypeResponse>> getAllDietType(int flag);

    ApiResponse<MasDietTypeResponse> findById(Long id);

    ApiResponse<MasDietTypeResponse> addDietType(MasDietTypeRequest request);

    ApiResponse<MasDietTypeResponse> update(Long id, MasDietTypeRequest request);

    ApiResponse<MasDietTypeResponse> changeStatus(Long id, String status);


}
