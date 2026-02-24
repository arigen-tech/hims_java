package com.hims.service;

import com.hims.request.LabInvestigationReq;
import com.hims.request.PatientRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.request.RadiologyReportRequest;
import com.hims.response.ApiResponse;
import com.hims.response.RadiologyAppSetupResponse;
import com.hims.response.RadiologyRequisitionResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RadiologyService {

    ApiResponse<RadiologyAppSetupResponse> registerPatientWithInv(PatientRequest patient, List<LabInvestigationReq> radInvestigationReq);
    @Transactional
    ApiResponse paymentStatusReq(PaymentUpdateRequest request);

    ApiResponse<Page<RadiologyRequisitionResponse>> pendingRadiology(Long modality, String patientName, String phoneNumber, int page, int size);


    ApiResponse<String> pendingInvestigationRadiology(Long id, String status);

    ApiResponse<Page<RadiologyRequisitionResponse>> getPendingReportRadiology(Long modality, String patientName, String phoneNumber, int page, int size);

    ApiResponse<String> add(RadiologyReportRequest request,String status);

    ApiResponse<Page<RadiologyRequisitionResponse>> getPACSStudyList(Long modality, String patientName, String phoneNumber, int page, int size);
}
