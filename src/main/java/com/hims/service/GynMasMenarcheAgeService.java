package com.hims.service;

import com.hims.request.GynMasMenarcheAgeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasMenarcheAgeResponse;

import java.util.List;

public interface GynMasMenarcheAgeService {

    ApiResponse<List<GynMasMenarcheAgeResponse>> getAll(int flag);

    ApiResponse<GynMasMenarcheAgeResponse> getById(Long id);

    ApiResponse<GynMasMenarcheAgeResponse> create(
            GynMasMenarcheAgeRequest request);

    ApiResponse<GynMasMenarcheAgeResponse> update(
            Long id, GynMasMenarcheAgeRequest request);

    ApiResponse<GynMasMenarcheAgeResponse> changeStatus(
            Long id, String status);
}
