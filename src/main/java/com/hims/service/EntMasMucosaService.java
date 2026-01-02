package com.hims.service;

import com.hims.request.EntMasMucosaRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasMucosaResponse;

import java.util.List;

public interface EntMasMucosaService {

    ApiResponse<List<EntMasMucosaResponse>> getAll(int flag);

    ApiResponse<EntMasMucosaResponse> getById(Long id);

    ApiResponse<EntMasMucosaResponse> create(EntMasMucosaRequest request);

    ApiResponse<EntMasMucosaResponse> update(Long id, EntMasMucosaRequest request);

    ApiResponse<EntMasMucosaResponse> changeStatus(Long id, String status);
}
