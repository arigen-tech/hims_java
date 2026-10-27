package com.hims.service;

import com.hims.request.ForgotPatientPasswordRequest;
import com.hims.request.PatientPasswordLoginRequest;
import com.hims.request.ResetPatientPasswordRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AuthResponse;
import com.hims.response.MobileLoginResponce;

public interface PatientPasswordService {
    ApiResponse<AuthResponse> login(PatientPasswordLoginRequest request);
    ApiResponse<MobileLoginResponce> requestPasswordReset(ForgotPatientPasswordRequest request);
    ApiResponse<String> resetPassword(ResetPatientPasswordRequest request);
}
