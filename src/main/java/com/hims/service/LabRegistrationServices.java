package com.hims.service;

import com.hims.request.LabRegRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.PaymentResponse;

public interface LabRegistrationServices {
    ApiResponse<AppsetupResponse> labReg(LabRegRequest labreq);

    ApiResponse<PaymentResponse> paymentStatusReq(PaymentUpdateRequest labreq);
}
