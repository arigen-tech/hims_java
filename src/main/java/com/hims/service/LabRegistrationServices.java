package com.hims.service;

import com.hims.request.AppointmentReq;
import com.hims.request.LabRegRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;

public interface LabRegistrationServices {
    ApiResponse<AppsetupResponse> labReg(LabRegRequest labreq);

    ApiResponse<AppsetupResponse> paymentStatusReq(PaymentUpdateRequest labreq);
}
