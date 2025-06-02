package com.hims.service;

import com.hims.request.MasStoreSectionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreSectionResponse;

import java.util.List;

public interface MasStoreSectionService {
    ApiResponse<MasStoreSectionResponse> addMasStoreSection(MasStoreSectionRequest masStoreSectionRequest);

    ApiResponse<List<MasStoreSectionResponse>> getAllStoreSection(int flag);

    ApiResponse<MasStoreSectionResponse> findById(Integer id);

    ApiResponse<MasStoreSectionResponse> changeStoreSectionStatus(int id, String status);

    ApiResponse<MasStoreSectionResponse> updateStoreSection(int id, MasStoreSectionRequest masStoreSectionRequest);
}
