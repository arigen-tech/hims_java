package com.hims.service;

import com.hims.request.*;
import com.hims.response.ApiResponse;
import com.hims.response.LabRadioUpdateResponse;
import com.hims.response.LabRadiologyRegistrationResponse;
import com.hims.response.RadiologyReportResponse;
import com.hims.response.RadiologyRequisitionResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RadiologyService {

    ApiResponse<LabRadiologyRegistrationResponse> registerPatientWithInv(PatientRequest patient, List<LabInvestigationReq> radInvestigationReq);

    ApiResponse<LabRadiologyRegistrationResponse> registerAndBookingRadiology(PatientRequest patient, List<LabRadioInvestigationRequest> investigationReq);

    LabRadioUpdateResponse updatePatientDetailsAndBooking(LabRadioUpdateRequest request);

    @Transactional
    ApiResponse paymentStatusReq(PaymentUpdateRequest request);

    ApiResponse<Page<RadiologyRequisitionResponse>> getPendingRadiology(Long modality, String patientName, String phoneNumber, int page, int size);


    ApiResponse<String> cancelOrCompleteInvestigationRadiology(Long id, String status);

    ApiResponse<Page<RadiologyRequisitionResponse>> getPendingListForRadiologyReport(Long modality, String patientName, String phoneNumber, int page, int size);

    ApiResponse<String> saveDetailsReportForRadiology(RadiologyReportRequest request,String status);
    ApiResponse<RadiologyReportResponse> getDetailsReportForRadiology(Long radOrderDtId);

    ApiResponse<Page<RadiologyRequisitionResponse>> getPACSStudyList(Long modality, String patientName, String phoneNumber, int page, int size);

}