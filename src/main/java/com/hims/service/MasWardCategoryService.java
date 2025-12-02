package com.hims.service;

import com.hims.request.MasWardCategoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasWardCategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MasWardCategoryService {
    ApiResponse<List<MasWardCategoryResponse>> getAllMasWardCategory(int flag);

    ApiResponse<MasWardCategoryResponse> findById(Long id);

    ApiResponse<MasWardCategoryResponse> addMasWard(MasWardCategoryRequest request);

    ApiResponse<MasWardCategoryResponse> update(Long id, MasWardCategoryRequest request);

    ApiResponse<MasWardCategoryResponse> changeMasWardStatus(Long id, String status);
}
