package com.hims.service;

import com.hims.request.*;
import com.hims.response.ApiResponse;
import com.hims.response.OpeningBalanceEntryResponse;
import com.hims.response.OpeningBalanceStockResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface OpeningBalanceEntryService {
    ApiResponse<OpeningBalanceEntryResponse> add(OpeningBalanceEntryRequest openingBalanceEntryRequest);


    ApiResponse<String> update(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest);

    ApiResponse<String> updateByStatus(Long id, String status);
    


    ApiResponse<OpeningBalanceEntryResponse> getDetailsById(Long id, Long hospitalId, Long departmentId);

    ApiResponse<OpeningBalanceEntryResponse> createAndUpdateStatus(OpeningBalanceEntryRequest request);

   List<OpeningBalanceEntryResponse> getListByStatus(List<String> statusList, Long hospitalId, Long departmentId);
    

    ApiResponse<String> approved(Long id, OpeningBalanceEntryRequest2 request);


    ApiResponse<List<?>> getAllStock(String type,Long hospitalId, Long departmentId);


    ApiResponse<List<OpeningBalanceStockResponseDto>> getStockByDateRange(LocalDate fromDate, LocalDate toDate, Long itemId, Long hospitalId, Long departmentId);

    ApiResponse<String> updateByMrp(List<UpdateMrpValue> marValue);

    ApiResponse<List<OpeningBalanceStockResponseDto>> getStockByItemId(Long itemId, Long hospitalId, Long departmentId);


}
