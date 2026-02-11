package com.hims.service;

import com.hims.request.LabInvestigationReq;
import com.hims.request.PatientRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.RadiologyAppSetupResponse;
import com.hims.response.RadiologyResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RadiologyService {

    ApiResponse<RadiologyAppSetupResponse> registerPatientWithInv(PatientRequest patient, List<LabInvestigationReq> radInvestigationReq);

    ApiResponse<List<RadiologyResponse>> pendingRadiology( Long modality);

    @Transactional
    ApiResponse paymentStatusReq(PaymentUpdateRequest request);
}
