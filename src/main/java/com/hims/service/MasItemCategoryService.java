package com.hims.service;

import com.hims.request.MasItemCategoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemCategoryResponse;

import java.util.List;

public interface MasItemCategoryService {
    ApiResponse<MasItemCategoryResponse> addMasItemCategory(MasItemCategoryRequest masItemCategoryRequest);

    ApiResponse<List<MasItemCategoryResponse>> getAllMasItemCategory(int flag);

    ApiResponse<MasItemCategoryResponse> findById(Integer id);

    ApiResponse<MasItemCategoryResponse> changeMasItemCategoryStatus(int id, String status);

    ApiResponse<MasItemCategoryResponse> updateMasItemClass(int id, MasItemCategoryRequest masItemCategoryRequest);

    ApiResponse<List<MasItemCategoryResponse>> findByMasItemCategoryBbySectionId(int id);
}
