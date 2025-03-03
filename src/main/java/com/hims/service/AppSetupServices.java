package com.hims.service;
import com.hims.request.AppointmentReq;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;

public interface AppSetupServices {
    ApiResponse<AppsetupResponse> appSetup(AppointmentReq appointmentReq);
}
