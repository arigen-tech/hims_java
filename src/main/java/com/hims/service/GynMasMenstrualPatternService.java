package com.hims.service;

import com.hims.request.GynMasMenstrualPatternRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasMenstrualPatternResponse;

import java.util.List;

public interface GynMasMenstrualPatternService {

    ApiResponse<List<GynMasMenstrualPatternResponse>> getAll(int flag);

    ApiResponse<GynMasMenstrualPatternResponse> getById(Long id);

    ApiResponse<GynMasMenstrualPatternResponse> create(
            GynMasMenstrualPatternRequest request);

    ApiResponse<GynMasMenstrualPatternResponse> update(
            Long id, GynMasMenstrualPatternRequest request);

    ApiResponse<GynMasMenstrualPatternResponse> changeStatus(
            Long id, String status);
}
