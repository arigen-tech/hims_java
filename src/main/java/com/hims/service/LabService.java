package com.hims.service;

import com.hims.request.InvestigationValidationRequest;
import com.hims.request.ResultUpdateRequest;
import com.hims.request.ResultValidationUpdateRequest;
import com.hims.request.SampleCollectionRequest;
import com.hims.response.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

public interface LabService {

    ApiResponse<Page<PendingSampleHeaderResponse>> getPendingSampleHeadersForCollection(Long hospitalId,String patientName,String patientMobileNumber,int page,int size);
    ApiResponse<List<PendingSampleDetailResponse>> getPendingSampleDetailsForCollection(Long orderHdId);
    ApiResponse<AppsetupResponse> savePendingSamplesForCollection(Long departmentId,SampleCollectionRequest request);
    ApiResponse<Page<SampleHeaderForValidationResponse>> getSampleHeaderForValidation(Long hospitalId, String patientName, String patientMobileNumber, int page, int size);
    ApiResponse<List<SampleDetailsForValidationResponse>> getSampleDetailsForValidationWrtHeader(Long sampleCollectionHeaderId);
    ApiResponse<String> validateInvestigations(List<InvestigationValidationRequest> requests);
    ApiResponse<Page<SampleHeaderForResultEntryResponse>> getSampleHeaderForResultEntry(Long hospitalId, String patientName, String patientMobileNumber, int page, int size);
    ApiResponse<List<InvestigationResultResponse>> getInvestigationsForResultEntry(Long sampleCollectionHeaderId);
    ApiResponse<List<SubInvestigationResultResponse>> getSubInvestigationsForResultEntry(Long investigationId,String genderCode,String age);
    ApiResponse<List<FixedValueResultResponse>> getFixedValuesResultDropdown(Long subInvestigationId);
    ApiResponse<Page<SampleHeaderForResultValidationResponse>> getSampleHeaderForResultValidation(Long hospitalId, String patientName, String patientMobileNumber, int page, int size) ;
    ApiResponse<List<InvestigationsForResultValidation>> getInvestigationsForResultValidation(Long resultEntryHeaderId) ;
    ApiResponse<List<SubInvestigationsForResultValidationResponse>> getSubInvestigationsForResultValidation(Long resultEntryDetailId,Long investigationId);
    ApiResponse<String> updateAndValidateResult( ResultValidationUpdateRequest request);
    ApiResponse<Page<ResultEntryHeaderForUpdateResponse>> getResultHeaderForUpdate(Long hospitalId,String patientName,String patientMobileNumber,int page,int size);
    ApiResponse<List<InvestigationsForResultUpdateResponse>> getInvestigationsForResultUpdate(Long orderHdId) ;
    ApiResponse<List<SubInvestigationsForResultValidationResponse>> getSubInvestigationsForResultUpdate(Long resultEntryDetailsId,Long investigationId);
    ApiResponse<String> updateResult(ResultUpdateRequest request);


    /* *********************************************   Report Section   **********************************************************************/


     ApiResponse<Page<LabInvestigationsReportResponse>> getAllInvestigationsReport(
                                                                                         Long hospitalId,
                                                                                         String mobileNo,
                                                                                         String patientName,
                                                                                         Long patientId,
                                                                                         LocalDate fromDate,
                                                                                         LocalDate toDate,
                                                                                         int page,
                                                                                         int size
    );

    ApiResponse<Page<LabDetailedTATReportResponse>> getDetailedTatReports(
            Long hospitalId,
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );

    ApiResponse<Page<LabSummaryTATReportResponse>> getSummaryTatReports(
            Long  hospitalId,
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size);

    ApiResponse<Page<LabAmenedAuditReportResponse>> getAmendAuditReports(
            Long hospitalId,
            String phnNum,
            String patientName,
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );


    ApiResponse<Page<OrderTrackingReportResponse>> getOrderTrackingReports(
            Long hospitalId,
            String patientName,
            String mobileNo,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );

    ApiResponse<Page<LabIncompleteInvestigationsReportResponse>> getIncompleteInvestigationReports(
            Long  hospitalId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size);


    ApiResponse<Page<SampleRejectionInvestigationReportResponse>> getSampleRejectionReport(
            Long hospitalId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size);





}
