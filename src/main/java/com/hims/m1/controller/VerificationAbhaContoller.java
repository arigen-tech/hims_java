package com.hims.m1.controller;

import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.request.VerificationRequest;
import com.hims.m1.request.VerifyIndexOTPRequest;
import com.hims.m1.request.VerifyOTPRequest;
import com.hims.m1.response.OtpSendResponse;
import com.hims.m1.response.OtpVerificationResponse;
import com.hims.m1.service.ApiControllerLogService;
import com.hims.m1.service.VerificationAbhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/abdm/verificationWeb")
@Tag(name = "ABHA Verification", description = "APIs for ABHA number verification using OTP-based authentication")
public class VerificationAbhaContoller {

    @Autowired
    private VerificationAbhaService verificationAbhaService;

    @Autowired
    private ApiControllerLogService apiControllerLogService;

    @Operation(summary = "Send OTP for ABHA Verification", description = "This API sends an OTP to the registered mobile number linked with the provided ABHA number or input type.")
    @PostMapping("/send-otp")
    public Mono<ResponseEntity<ApiResponse<OtpSendResponse>>> abhaVerification(@Valid @RequestBody VerificationRequest verificationRequest) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/verificationWeb/send-otp",
                verificationRequest,
                verificationAbhaService.verificationByNumber(verificationRequest).map(ResponseEntity::ok)
        );
    }

    @Operation(summary = "Verify OTP for ABHA Authentication", description = "This API verifies the OTP sent to the registered mobile number and completes the ABHA authentication process.")
    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<ApiResponse<OtpVerificationResponse>>> verifyOtpWithMobileNumber(@Valid @RequestBody VerifyOTPRequest verifyOTPRequest) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/verificationWeb/verify-otp",
                verifyOTPRequest,
                verificationAbhaService.verifyOtpWithMobileNumber(verifyOTPRequest).map(ResponseEntity::ok)
        );
    }

    @Operation(summary = "Verify OTP for INDEX number", description = "This API verifies the OTP sent to the registered mobile number and completes the ABHA authentication process.")
    @PostMapping("/index-otp")
    public Mono<ResponseEntity<ApiResponse<OtpSendResponse>>> verifyOtpWithIndexNumber(@Valid @RequestBody VerifyIndexOTPRequest verifyOTPRequest) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/verificationWeb/index-otp",
                verifyOTPRequest,
                verificationAbhaService.verifyOtpWithMobileNumber(verifyOTPRequest).map(ResponseEntity::ok)
        );
    }

    @Operation(summary = "Verify OTP for INDEX number", description = "This API verifies the OTP sent to the registered mobile number and completes the ABHA authentication process.")
    @PostMapping("/send-otp-usingAuth")
    public Mono<ResponseEntity<ApiResponse<OtpSendResponse>>> sendOtpUsingAuth(@Valid @RequestBody VerificationRequest verifyOTPRequest) throws Exception {
        return apiControllerLogService.logMonoApi(
                "send-otp-usingAuth",
                verifyOTPRequest,
                verificationAbhaService.sendOtpUsingAuth(verifyOTPRequest).map(ResponseEntity::ok)
        );
    }
}
