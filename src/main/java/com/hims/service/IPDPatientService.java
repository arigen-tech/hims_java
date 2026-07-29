package com.hims.service;

import com.hims.request.*;
import com.hims.response.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IPDPatientService {
    ApiResponse<Page<IPDPatientWaitingListResponse>> pendingAdmissionList(
            int page,
            int size,
            Long hospitalId,
            String patientName,
            String mobileNo
    );

    ApiResponse<String> saveAdmissionDetails(IpdPatientRequest request);

    ApiResponse<List<IpdWardResponse>> getWardDetailsByDepartment(Long departmentId);

    ApiResponse<List<IpdRoomResponse>> getRoomDetailsByWard(Long wardId);

    ApiResponse<List<WardResponse>> getWardDetailsByCategory(Long wardCategoryId);

    ApiResponse<List<BedResponse>> getBedDetailsByRoom(Long roomId);

    ApiResponse<List<WardWiseDetailsResponse>> getNursingDashboardByWard(Long wardId);

    ApiResponse<TotalBedCountResponse> getTotalBedCountByWard(Long wardId);

    ApiResponse<String> saveNursingMedicalAssessment(IpNursingMedicalAssessmentRequest request);

    ApiResponse<String> updateAdmissionInternalStatus(Long inpatientId,Long internalStatusId);

    ApiResponse<List<IpVitalsResponse>> getVitalsDetails(Long inpatientId);

    ApiResponse<String> saveVitalsDetails(IpVitalsRequest request);

    ApiResponse<String> saveIntakeOutputDetails(@Valid IpIntakeOutputSaveRequest request);

    ApiResponse<String> saveDailyCaseSheetEntry(@Valid IpDailyCaseSheetEntryRequest request);

    ApiResponse<List<DailyCaseSheetEntryResponse>> getDailyCaseSheetEntry(Long inpatientId);

    ApiResponse<List<BedDetailsByWardResponse>> getBedDetailsByWard(Long wardId);

    ApiResponse<String> saveBedTransferRequest(@Valid BedTransferRequest request);

    ApiResponse<List<PendingToTransferResponse>> wardPendingToTransferRequest(List<Long> wardIds);

    ApiResponse<String> wardPendingToTransferRequestStatusCompleteAndReject(Long inpatientId,String transferStatus);

    ApiResponse<String> saveInpatientBookingInvestigation(@Valid InpatientBookingInvestigationRequest request);

    ApiResponse<List<PendingToTransferResponse>> wardTransferList( List<Long> wardIds);

   ApiResponse<String> saveIpDiagnosisEntry(@Valid IpDiagnosisEntryRequest request);

    ApiResponse<List<IpDiagnosisEntryResponse>> getIpDiagnosisEntry(Long inpatientID);

    ApiResponse<List<IntakeOutputResponse>> getIntakeOutputDetails(Long inpatientID);

    ApiResponse<String> saveDischargeSummary(@Valid IpDischargeSummarySaveRequest request);

    ApiResponse<PaymentStatusResponse> getPaymentStatus(Long inpatientID);
}
