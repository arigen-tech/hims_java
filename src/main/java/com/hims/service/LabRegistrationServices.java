package com.hims.service;

import com.hims.request.*;
import com.hims.response.*;

import java.time.LocalDate;
import java.util.List;

public interface LabRegistrationServices {
    ApiResponse<AppsetupResponse> labReg(LabRegRequest labreq);
    ApiResponse<PaymentResponse> paymentStatusReq(PaymentUpdateRequest labreq);

    ApiResponse<LabRadiologyRegistrationResponse> registerAndBookingLaboratory(LabRadioRegistrationRequest investigationReq);
    ApiResponse<AppsetupResponse> updateDetailsAndBookingLaboratory(LabRadioUpdateRequest labreq);

    List<PendingSampleResponse> getPendingSamples();
    ApiResponse<AppsetupResponse> savesample(SampleCollectionRequest labreq);
    ApiResponse<AppsetupResponse> labRegForExistingOrder(LabBillingOnlyRequest labReq);
//    ApiResponse<Boolean> findDuplicateInvestigationAndPackage(Long investigationId,Long packageId,List<Long> packageList, List<Long> investigationList, LocalDate date);

}
