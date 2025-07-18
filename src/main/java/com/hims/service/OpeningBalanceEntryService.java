package com.hims.service;

import com.hims.entity.StoreItemBatchStock;
import com.hims.request.*;
import com.hims.response.ApiResponse;
import com.hims.response.OpeningBalanceEntryResponse;
import com.hims.response.OpeningBalanceStockResponse2;
import com.hims.response.StoreStockTakingTResponse;

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


    ApiResponse<List<OpeningBalanceStockResponse2 >> getStockByDateRange(LocalDate fromDate, LocalDate toDate,Long itemId);

    ApiResponse<String> updateByMrp(List<UpdateMrpValue> marValue);

    ApiResponse<List<OpeningBalanceStockResponse2>> getStockByItemId(Long itemId);


}
