package com.hims.service;

import com.hims.response.AllLabReportResponse;
import com.hims.response.ApiResponse;
import com.hims.response.LabDetailedTATReportResponse;
import com.hims.response.LabSummaryTATReportResponse;

import java.time.LocalDate;
import java.util.List;

public interface LabReportService {

    ApiResponse<List<AllLabReportResponse>> getAllLabReports(String phnNum, String patientName, LocalDate fromDate,LocalDate toDate);
    ApiResponse<List<LabDetailedTATReportResponse>> getDetailedTatReports(Long investigationId, Long subChargeCodeId, LocalDate fromDate, LocalDate toDate);
    ApiResponse<List<LabSummaryTATReportResponse>> getSummaryTatReports(Long investigationId, Long subChargeCodeId, LocalDate fromDate, LocalDate toDate);

}
