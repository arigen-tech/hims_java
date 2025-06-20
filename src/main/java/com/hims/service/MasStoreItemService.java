package com.hims.service;

import com.hims.entity.MasStoreItem;
import com.hims.request.MasStoreItemRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreItemResponse;

import java.util.List;

public interface MasStoreItemService {
    ApiResponse<MasStoreItemResponse> addMasStoreItem(MasStoreItemRequest masStoreItemRequest);


    ApiResponse<MasStoreItemResponse> findById(Long id);

    ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItem(int flag);

    ApiResponse<MasStoreItemResponse> update(Long id, MasStoreItemRequest request);

    ApiResponse<MasStoreItemResponse> changeMasStoreItemStatus(Long id, String status);
}
