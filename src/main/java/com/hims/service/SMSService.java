package com.hims.service;

import com.hims.response.ApiResponse;

public interface SMSService {

     ApiResponse<String> sendAppointmentCancellationInOtp(String mobileNumber);
     ApiResponse<Boolean> verifyOtp(String sessionId,String otp);
}
