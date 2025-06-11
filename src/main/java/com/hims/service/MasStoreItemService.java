package com.hims.service;

import com.hims.entity.MasStoreItem;
import com.hims.request.MasStoreItemRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreItemResponse;

import java.util.List;

public interface MasStoreItemService {
    ApiResponse<MasStoreItemResponse> addMasStoreItem(MasStoreItemRequest masStoreItemRequest);


    ApiResponse<MasStoreItemResponse> findById(Integer id);

    ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItem(int flag);
}
