package com.hims.service;

import com.hims.request.LabBillingOnlyRequest;
import com.hims.request.LabRegRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.request.SampleCollectionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.PaymentResponse;
import com.hims.response.PendingSampleResponse;

import java.time.LocalDate;
import java.util.List;

public interface LabRegistrationServices {
    ApiResponse<AppsetupResponse> labReg(LabRegRequest labreq);

    ApiResponse<PaymentResponse> paymentStatusReq(PaymentUpdateRequest labreq);

    List<PendingSampleResponse> getPendingSamples();
    ApiResponse<AppsetupResponse> savesample(SampleCollectionRequest labreq);
    ApiResponse<AppsetupResponse> labRegForExistingOrder(LabBillingOnlyRequest labReq);
//    ApiResponse<Boolean> findDuplicateInvestigationAndPackage(Long investigationId,Long packageId,List<Long> packageList, List<Long> investigationList, LocalDate date);
}
