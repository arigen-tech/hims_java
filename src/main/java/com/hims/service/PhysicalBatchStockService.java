package com.hims.service;

import com.hims.request.StoreStockTakingMRequest;
import com.hims.request.StoreStockTakingMRequest2;
import com.hims.response.ApiResponse;
import com.hims.response.StoreStockTakingMResponse;
import com.hims.response.StoreStockTakingTResponse;

import java.util.List;

public interface PhysicalBatchStockService {
    ApiResponse<String> createPhysicalStock(StoreStockTakingMRequest storeStockTakingM);

    List<StoreStockTakingMResponse> getListByStatusPhysical(String[] statuses);

    ApiResponse<String> updateByStatus(Long id, String status);

    ApiResponse<String> updatePhysicalById(Long id, StoreStockTakingMRequest storeStockTakingMRequest);

    ApiResponse<String> approvedPhysical( StoreStockTakingMRequest2 request);
}
