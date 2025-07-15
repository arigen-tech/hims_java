package com.hims.service;

import com.hims.entity.StoreItemBatchStock;
import com.hims.request.OpeningBalanceDtRequest;
import com.hims.request.OpeningBalanceEntryRequest;
import com.hims.request.OpeningBalanceEntryRequest2;
import com.hims.request.StoreItemBatchStockRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpeningBalanceEntryResponse;
import com.hims.response.OpeningBalanceStockResponse2;

import java.time.LocalDate;
import java.util.List;

public interface OpeningBalanceEntryService {
    ApiResponse<OpeningBalanceEntryResponse> add(OpeningBalanceEntryRequest openingBalanceEntryRequest);


    ApiResponse<String> update(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest);

    ApiResponse<String> updateByStatus(Long id, String status);
    


    ApiResponse<OpeningBalanceEntryResponse> getDetailsById(Long id);

    ApiResponse<OpeningBalanceEntryResponse> createAndUpdateStatus(OpeningBalanceEntryRequest request);

    List<OpeningBalanceEntryResponse> getListByStatus(String[] statuses);
    

    ApiResponse<String> approved(Long id, OpeningBalanceEntryRequest2 request);


    ApiResponse<List<?>> getAllStock(String type);


    ApiResponse<String> updateByMrp(Long id, StoreItemBatchStockRequest storeItemBatchStockRequest);

    ApiResponse<List<OpeningBalanceStockResponse2 >> getStockByDateRange(LocalDate fromDate, LocalDate toDate);
}
