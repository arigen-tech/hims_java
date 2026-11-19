package com.hims.service;


import com.hims.request.LoginRequest;
import com.hims.request.PatientActivationOtpRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AuthResponse;
import com.hims.response.MobileLoginResponce;


public interface MobileLoginService {
    ApiResponse<MobileLoginResponce> loginRequest(LoginRequest request);

    ApiResponse<AuthResponse> switchPatient(Long patientId, String mobileNumber);

    ApiResponse<String> deactivatePatient(Long patientId);

    ApiResponse<MobileLoginResponce> sendPatientActivationOtp(PatientActivationOtpRequest request);

    ApiResponse<String> verifyPatientActivationOtp(PatientActivationOtpRequest request);


}
