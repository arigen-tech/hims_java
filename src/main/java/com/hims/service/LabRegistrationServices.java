package com.hims.service;

import com.hims.request.LabRegRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.request.SampleCollectionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.PaymentResponse;
import com.hims.response.PendingSampleResponse;

import java.util.List;

public interface LabRegistrationServices {
    ApiResponse<AppsetupResponse> labReg(LabRegRequest labreq);

    ApiResponse<PaymentResponse> paymentStatusReq(PaymentUpdateRequest labreq);

    List<PendingSampleResponse> getPendingSamples();
    ApiResponse<AppsetupResponse> savesample(SampleCollectionRequest labreq);
}
