package com.hims.service;

import com.hims.request.MasMealTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMealTypeResponse;

import java.util.List;

public interface MasMealTypeService {
    ApiResponse<List<MasMealTypeResponse>> getAllMealType(int flag);

    ApiResponse<MasMealTypeResponse> findById(Long id);

    ApiResponse<MasMealTypeResponse> addMealType(MasMealTypeRequest request);

    ApiResponse<MasMealTypeResponse> update(Long id, MasMealTypeRequest request);

    ApiResponse<MasMealTypeResponse> changeStatus(Long id, String status);
}
