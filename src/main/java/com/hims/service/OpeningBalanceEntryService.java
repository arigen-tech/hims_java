package com.hims.service;

import com.hims.request.OpeningBalanceEntryRequest;
import com.hims.request.OpeningBalanceEntryRequest2;
import com.hims.response.ApiResponse;
import com.hims.response.OpeningBalanceEntryResponse;
import com.hims.response.OpeningBalanceEntryResponse2;

import java.util.List;

public interface OpeningBalanceEntryService {
    ApiResponse<OpeningBalanceEntryResponse> add(OpeningBalanceEntryRequest openingBalanceEntryRequest);


    ApiResponse<OpeningBalanceEntryResponse> update(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest);

    ApiResponse<String> updateByStatus(Long id, String status);
    


    ApiResponse<OpeningBalanceEntryResponse> getDetailsById(Long id);

    ApiResponse<OpeningBalanceEntryResponse> createAndUpdateStatus(OpeningBalanceEntryRequest request);

    List<OpeningBalanceEntryResponse> getListByStatus(String[] statuses);

    ApiResponse<String> approved(Long id,OpeningBalanceEntryRequest2 request);
}
