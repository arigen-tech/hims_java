package com.hims.service;

import com.hims.request.MasCareLevelRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCareLevelResponse;

import java.util.List;

public interface MasCareLevelService {

    ApiResponse<MasCareLevelResponse> createCareLevel(MasCareLevelRequest request);
    ApiResponse<MasCareLevelResponse> updateCareLevel(Long careId,MasCareLevelRequest request);
    ApiResponse<MasCareLevelResponse> changeActiveStatus(Long careId,String status);
    ApiResponse<MasCareLevelResponse> getById(Long careId);
    ApiResponse<List<MasCareLevelResponse>> getAll(int flag);

}
