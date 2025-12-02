package com.hims.service;

import com.hims.request.MasRoomCategoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRoomCategoryResponse;

import java.util.List;

public interface MasRoomCategoryService {

    ApiResponse<MasRoomCategoryResponse> createRoomCategory(MasRoomCategoryRequest request);

    ApiResponse<MasRoomCategoryResponse> updateRoomCategory(Long roomCategoryId, MasRoomCategoryRequest request);

    ApiResponse<MasRoomCategoryResponse> changeActiveStatus(Long roomCategoryId, String status);

    ApiResponse<MasRoomCategoryResponse> getById(Long roomCategoryId);

    ApiResponse<List<MasRoomCategoryResponse>> getAll(int flag);
}
