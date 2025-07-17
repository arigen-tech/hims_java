package com.hims.service;

import com.hims.request.StoreStockTakingMRequest;
import com.hims.response.ApiResponse;

public interface PhysicalBatchStockService {
    ApiResponse<String> createPhysicalStock(StoreStockTakingMRequest storeStockTakingM);
}
