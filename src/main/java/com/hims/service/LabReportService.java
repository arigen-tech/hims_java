package com.hims.service;

import com.hims.response.*;

import java.time.LocalDate;
import java.util.List;

public interface LabReportService {

    ApiResponse<List<AllLabReportResponse>> getAllLabReports(String phnNum, String patientName, LocalDate fromDate,LocalDate toDate);
    ApiResponse<List<LabDetailedTATReportResponse>> getDetailedTatReports(Long investigationId, Long subChargeCodeId, LocalDate fromDate, LocalDate toDate);
    ApiResponse<List<LabSummaryTATReportResponse>> getSummaryTatReports(Long investigationId, Long subChargeCodeId, LocalDate fromDate, LocalDate toDate);
    ApiResponse<List<LabAmenedAuditReportResponse>> getAmendAuditReports(String phnNum,String patientName,Long investigationId,Long subChargeCodeId,LocalDate fromDate,LocalDate toDate);
    ApiResponse<List<OrderTrackingReportResponse>> getOrderTrackingReports(String patientName,String phnNum,LocalDate fromDate,LocalDate toDate);
    ApiResponse<List<LabIncompleteInvestigationsReportResponse>> getIncompleteInvestigationReports(Long subChargeCodeId,LocalDate fromDate,LocalDate toDate);
    ApiResponse<List<SampleRejectionInvestigationReportResponse>> getSampleRejectionReport(Long modalityId,LocalDate fromDate,LocalDate toDate);
}
