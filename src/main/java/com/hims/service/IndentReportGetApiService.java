package com.hims.service;

import com.hims.response.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface IndentReportGetApiService {

    ApiResponse<Page<IndentTrackingListReportResponse>> getIndentTrackingList(int page,int size);
    ApiResponse<Page<IndentTrackingListReportResponse>> searchIndentTrackingList(
                                                                                        Long fromDepartmentId,
                                                                                        LocalDate fromDate,
                                                                                        LocalDate toDate,
                                                                                        int page,
                                                                                        int size
                                                                                );
    ApiResponse<List<MasCommonStatusResponse>> getStatusMapForIndentTracking();

    ApiResponse<Long> getIssueMIdFromIndentMId(Long indentMId);
    ApiResponse<Long> getReceiveMIdFromIndentMId(Long indentMId);
    ApiResponse<Long> getReturnMIdFromIndentMId(Long indentMId);
    ApiResponse<Page<ItemStockLedgerWithBatchResponse>> getStoreItems(String keyword,int page,int size);
    ApiResponse<List<String>> getBatchesFromItemId(Long itemId);
    ApiResponse<Page<StoreStockLedgerReportResponse>> getStoreStockLedgerReport(int page,int size,Long itemId, String batchNo);
}
