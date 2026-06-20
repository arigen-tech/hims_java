package com.hims.m1.service;


import com.hims.m1.abdm_response.AbdmUpdateAddressResponse;
import com.hims.m1.abdm_response.GetAbhaDetails.AbhaProfileResponse;
import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.request.*;
import com.hims.m1.response.*;
import reactor.core.publisher.Mono;

public interface CreateAbhaService {

    Mono<ApiResponse<OtpSendResponse>> sendOtpToAdhar(CreateAbhaSendOTPRequest verificationRequest) throws Exception;
    Mono<ApiResponse<CreateAbhaResponse>> createAbhaForVeryingOtp(VerifingAbhaSendOTPRequest verifyOTPRequest) throws Exception;
    Mono<ApiResponse<CreateAbhaResponse>> verifyOtpToAadhaarAnotherNumber(VerifingAbhaSendOTPAnotherRequest verifyOTPRequest) throws Exception;

    Mono<ApiResponse<CreateAbhaResponse>> verifyEmail(UpdateEmailVerificationRequest verifyOTPRequest) throws Exception;
    Mono<ApiResponse<AbhaSuggetionResponse>> abhaAddressSuggestion(AbhaSuggestionRequest verifyOTPRequest) throws Exception;
    Mono<ApiResponse<DownlaodAbhaCardResponse>> abhaCardDownload(AbhaDownlaodAndDetailsRequest verifyOTPRequest) throws Exception;
    Mono<ApiResponse<GetAbhaProfileResponse>> getProfileDetails(AbhaDownlaodAndDetailsRequest verifyOTPRequest) throws Exception;


    Mono<ApiResponse<OtpSendResponse>> updateEmailSendOtp(UpdateEmailVerificationRequest verifyOTPRequest) throws Exception;
    Mono<ApiResponse<UpdateEmailResponse>> updateEmailVerifyOtp(UpdateVerifyRequest verifyOTPRequest) throws Exception;

    Mono<ApiResponse<OtpSendResponse>> updateMobileSendOtp(UpdateMobileVerificationRequest verifyOTPRequest) throws Exception;
    Mono<ApiResponse<AbdmUpdateAddressResponse>> updateSuggestion(AbhaSuggestionUpdateRequest verifyOTPRequest) throws Exception;
    Mono<ApiResponse<AbhaProfileResponse>> updateProfilePhoto(UpdateProfileRequest updateProfileRequest) throws Exception;
    Mono<ApiResponse<UpdateEmailResponse>> updateMobileVerifyOtp(UpdateVerifyRequest verifyOTPRequest) throws Exception;

}
