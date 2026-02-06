package com.hims.service;

import com.hims.request.LabInvestigationReq;
import com.hims.request.PatientRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.RadiologyAppSetupResponse;
import com.hims.response.RadiologyResponse;

import java.util.List;

public interface RadiologyService {

    ApiResponse<RadiologyAppSetupResponse> registerPatientWithInv(PatientRequest patient, List<LabInvestigationReq> radInvestigationReq);

    ApiResponse<List<RadiologyResponse>> pendingRadiology(Long patientId, Long modality);
}
