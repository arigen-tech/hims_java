package com.hims.service;

import com.hims.entity.StoreItemBatchStock;
import com.hims.request.OpeningBalanceDtRequest;
import com.hims.request.OpeningBalanceEntryRequest;
import com.hims.request.OpeningBalanceEntryRequest2;
import com.hims.response.ApiResponse;
import com.hims.response.OpeningBalanceEntryResponse;

import java.util.List;

public interface OpeningBalanceEntryService {
    ApiResponse<OpeningBalanceEntryResponse> add(OpeningBalanceEntryRequest openingBalanceEntryRequest);


    ApiResponse<String> update(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest);

    ApiResponse<String> updateByStatus(Long id, String status);
    


    ApiResponse<OpeningBalanceEntryResponse> getDetailsById(Long id);

    ApiResponse<OpeningBalanceEntryResponse> createAndUpdateStatus(OpeningBalanceEntryRequest request);

    List<OpeningBalanceEntryResponse> getListByStatus(String[] statuses);

    ApiResponse<List<StoreItemBatchStock>> getAllStock();

    ApiResponse<String> approved(Long id, OpeningBalanceEntryRequest2 request);


}
