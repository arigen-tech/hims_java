package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.IndentTrackingListReportResponse;
import com.hims.response.MasCommonStatusResponse;
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
}
