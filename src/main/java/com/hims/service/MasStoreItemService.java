package com.hims.service;

import com.hims.projection.ItemProjection;
import com.hims.request.MasStoreItemRequest;
import com.hims.response.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MasStoreItemService {
    ApiResponse<MasStoreItemResponse> addMasStoreItem(MasStoreItemRequest masStoreItemRequest);


    ApiResponse<MasStoreItemResponse> findById(Long id);

    ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItem(int flag);

    ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItemWithotStock(int flag);

    ApiResponse<MasStoreItemResponse> update(Long id, MasStoreItemRequest request);

    ApiResponse<MasStoreItemResponse> changeMasStoreItemStatus(Long id, String status);

    ApiResponse<MasStoreItemResponse> findByCode(String code);

    ApiResponse<List<MasStoreItemResponseDto>> getAllMasStore(int flag);

    public ApiResponse<List<MasStoreItemResponseWithStock>> getAllMasStoreItemBySectionOnly(int flag);

    ApiResponse<Page<MasStoreItemResponseWithStock>> getMasStoreItemDynamic(
            int flag,
            String search,
            int page,
            int size);

    ApiResponse<List<ItemProjection>> getAllDrugs(Integer sectionId);

    ApiResponse<Page<ItemStockLedgerWithBatchResponse>> getStoreItems(String keyword, int page, int size);
}

