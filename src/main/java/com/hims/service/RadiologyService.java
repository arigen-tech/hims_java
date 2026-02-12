package com.hims.service;

import com.hims.request.LabInvestigationReq;
import com.hims.request.PatientRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.RadiologyAppSetupResponse;
import com.hims.response.RadiologyRequisitionResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RadiologyService {

    ApiResponse<RadiologyAppSetupResponse> registerPatientWithInv(PatientRequest patient, List<LabInvestigationReq> radInvestigationReq);
    @Transactional
    ApiResponse paymentStatusReq(PaymentUpdateRequest request);

    ApiResponse<Page<RadiologyRequisitionResponse>> pendingRadiology(Long modality, String patientName, String phoneNumber, int page, int size);


}
