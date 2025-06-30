package com.hims.service;

import com.hims.request.OpeningBalanceEntryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpeningBalanceEntryResponse;

import java.util.List;

public interface OpeningBalanceEntryService {
    ApiResponse<OpeningBalanceEntryResponse> add(OpeningBalanceEntryRequest openingBalanceEntryRequest);


    ApiResponse<OpeningBalanceEntryResponse> update(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest);

    ApiResponse<String> updateByStatus(Long id, String status);

    ApiResponse<List<OpeningBalanceEntryResponse>> getListByStatus(String status);


    ApiResponse<OpeningBalanceEntryResponse> getDetailsById(Long id);

    ApiResponse<OpeningBalanceEntryResponse> createAndUpdateStatus(OpeningBalanceEntryRequest request);
}
