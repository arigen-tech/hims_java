package com.hims.service;

import com.hims.entity.MasStoreItem;
import com.hims.request.MasStoreItemRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreItemResponse;
import com.hims.response.MasStoreItemResponse2;

import java.util.List;

public interface MasStoreItemService {
    ApiResponse<MasStoreItemResponse> addMasStoreItem(MasStoreItemRequest masStoreItemRequest);


    ApiResponse<MasStoreItemResponse> findById(Long id);

    ApiResponse<List<MasStoreItemResponse2>> getAllMasStoreItem(int flag);

    ApiResponse<MasStoreItemResponse> update(Long id, MasStoreItemRequest request);

    ApiResponse<MasStoreItemResponse> changeMasStoreItemStatus(Long id, String status);

    ApiResponse<MasStoreItemResponse> findByCode(String code);

    ApiResponse<List<MasStoreItemResponse2>> getAllMasStore(int flag);
}
