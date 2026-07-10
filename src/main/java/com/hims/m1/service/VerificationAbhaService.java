package com.hims.m1.service;


import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.request.VerificationRequest;
import com.hims.m1.request.VerifyIndexOTPRequest;
import com.hims.m1.request.VerifyOTPRequest;
import com.hims.m1.response.OtpSendResponse;
import com.hims.m1.response.OtpVerificationResponse;
import reactor.core.publisher.Mono;

public interface VerificationAbhaService {

    Mono<ApiResponse<OtpSendResponse>> verificationByNumber(VerificationRequest verificationRequest) throws Exception;
    Mono<ApiResponse<OtpSendResponse>> sendOtpUsingAuth(VerificationRequest verificationRequest) throws Exception;
    Mono<ApiResponse<OtpVerificationResponse>> verifyOtpWithMobileNumber(VerifyOTPRequest verifyOTPRequest) throws Exception;
    Mono<ApiResponse<OtpSendResponse>> verifyOtpWithMobileNumber(VerifyIndexOTPRequest verifyOTPRequest) throws Exception;

}
