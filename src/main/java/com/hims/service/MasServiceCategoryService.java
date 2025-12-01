package com.hims.service;

import com.hims.entity.MasServiceCategory;
import com.hims.response.ApiResponse;
import com.hims.response.GstConfigResponse;

import java.util.List;

public interface MasServiceCategoryService {
    ApiResponse<List<MasServiceCategory>> findAll(int flag);
    ApiResponse<MasServiceCategory> save(MasServiceCategory req);
    ApiResponse<MasServiceCategory> edit(Long id, MasServiceCategory req);

    ApiResponse<MasServiceCategory> updateStatus(Long id, String status);

    ApiResponse<GstConfigResponse> getGstConfig(int flag , Integer catId);
}
