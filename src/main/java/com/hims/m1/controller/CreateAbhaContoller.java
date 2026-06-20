package com.hims.m1.controller;

import com.hims.m1.abdm_response.AbdmUpdateAddressResponse;
import com.hims.m1.abdm_response.GetAbhaDetails.AbhaProfileResponse;
import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.request.*;
import com.hims.m1.response.*;
import com.hims.m1.service.ApiControllerLogService;
import com.hims.m1.service.CreateAbhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/abdm/create")
@CrossOrigin(origins = "*")
@Tag(name = "ABHA Creation", description = "APIs to create ABHA using Aadhaar OTP, manage ABHA profile, and update contact details")

public class CreateAbhaContoller {

    @Autowired
    private CreateAbhaService createAbhaService;

    @Autowired
    private ApiControllerLogService apiControllerLogService;

    // -------------------- Aadhaar OTP Flow --------------------

    @Operation(summary = "Send Aadhaar OTP for ABHA Creation", description = "This API sends an OTP to the mobile number linked with Aadhaar to initiate ABHA creation.")
    @PostMapping("/send-otp-aadhaar")
    public Mono<ResponseEntity<ApiResponse<OtpSendResponse>>> sendOtpToAadhaar(@Valid @RequestBody CreateAbhaSendOTPRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/send-otp-aadhaar",
                request,
                createAbhaService.sendOtpToAdhar(request).map(ResponseEntity::ok)
        );
    }

    @Operation(summary = "Verify Aadhaar OTP and Create ABHA", description = "This API verifies the Aadhaar OTP and creates a new ABHA profile."
    )
    @PostMapping("/verify-otp-aadhaar")
    public Mono<ResponseEntity<ApiResponse<CreateAbhaResponse>>> verifyAadhaarOtp(@Valid @RequestBody VerifingAbhaSendOTPRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/verify-otp-aadhaar",
                request,
                createAbhaService.createAbhaForVeryingOtp(request).map(ResponseEntity::ok)
        );
    }


    @Operation(summary = "Verify Aadhaar OTP for another mobile number", description = "This API verifies the OTP sent to the Aadhaar-linked mobile number when an alternate mobile number is used.")
    @PostMapping("/verify-otp-aadhaar-another-number")
    public Mono<ResponseEntity<ApiResponse<CreateAbhaResponse>>> verifyOtpToAadhaarAnotherNumber(@Valid @RequestBody VerifingAbhaSendOTPAnotherRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/verify-otp-aadhaar-another-number",
                request,
                createAbhaService.verifyOtpToAadhaarAnotherNumber(request).map(ResponseEntity::ok)
        );
    }


    // -------------------- Email Verification --------------------

    @Operation(summary = "Verify Email for ABHA Profile", description = "This API verifies the email address linked with the ABHA profile.")
    @PostMapping("/verify-email")
    public Mono<ResponseEntity<ApiResponse<CreateAbhaResponse>>> verifyEmail(@Valid @RequestBody UpdateEmailVerificationRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/verify-email",
                request,
                createAbhaService.verifyEmail(request).map(ResponseEntity::ok)
        );
    }


    // -------------------- ABHA Address & Profile --------------------

    @Operation(summary = "Get ABHA Address Suggestions", description = "This API provides available ABHA address suggestions based on user input.")
    @PostMapping("/abha-address-suggestion")
    public Mono<ResponseEntity<ApiResponse<AbhaSuggetionResponse>>> abhaAddressSuggestion(@Valid @RequestBody AbhaSuggestionRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/abha-address-suggestion",
                request,
                createAbhaService.abhaAddressSuggestion(request).map(ResponseEntity::ok)
        );
    }

    @Operation(summary = "Fetch ABHA Profile Details", description = "This API fetches detailed ABHA profile information for the authenticated user.")
    @PostMapping("/abha-details")
    public Mono<ResponseEntity<ApiResponse<GetAbhaProfileResponse>>> getProfileDetails(@Valid @RequestBody AbhaDownlaodAndDetailsRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/abha-details",
                request,
                createAbhaService.getProfileDetails(request).map(ResponseEntity::ok)
        );
    }


    @Operation(summary = "Download ABHA Card", description = "This API downloads the ABHA card for the authenticated user.")
    @PostMapping("/abha-download")
    public Mono<ResponseEntity<ApiResponse<DownlaodAbhaCardResponse>>> abhaCardDownload(@Valid @RequestBody AbhaDownlaodAndDetailsRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/abha-download",
                request,
                createAbhaService.abhaCardDownload(request).map(ResponseEntity::ok)
        );
    }


    // -------------------- Update Email --------------------

    @Operation(summary = "Send OTP to Update Email", description = "This API sends an OTP to the new email address to initiate email update.")
    @PostMapping("/update-email-sendOtp")
    public Mono<ResponseEntity<ApiResponse<OtpSendResponse>>> updateEmailSendOtp(@Valid @RequestBody UpdateEmailVerificationRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/update-email-sendOtp",
                request,
                createAbhaService.updateEmailSendOtp(request).map(ResponseEntity::ok)
        );
    }

    @Operation(summary = "Verify OTP and Update Email", description = "This API verifies the OTP and updates the email address linked with ABHA.")
    @PostMapping("/update-email-verifyOtp")
    public Mono<ResponseEntity<ApiResponse<UpdateEmailResponse>>> updateEmailVerifyOtp(@Valid @RequestBody UpdateVerifyRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/update-email-verifyOtp",
                request,
                createAbhaService.updateEmailVerifyOtp(request).map(ResponseEntity::ok)
        );
    }


    // -------------------- Update Mobile --------------------

    @Operation(summary = "Send OTP to Update Mobile Number", description = "This API sends an OTP to the new mobile number to initiate mobile update.")
    @PostMapping("/update-mobile-sendOtp")
    public Mono<ResponseEntity<ApiResponse<OtpSendResponse>>> updateMobileSendOtp(@Valid @RequestBody UpdateMobileVerificationRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/update-mobile-sendOtp",
                request,
                createAbhaService.updateMobileSendOtp(request).map(ResponseEntity::ok)
        );
    }

    @Operation(summary = "Verify OTP and Update Mobile Number", description = "This API verifies the OTP and updates the mobile number linked with ABHA.")
    @PostMapping("/update-mobile-verifyOtp")
    public Mono<ResponseEntity<ApiResponse<UpdateEmailResponse>>> updateMobileVerifyOtp(@Valid @RequestBody UpdateVerifyRequest request) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/update-mobile-verifyOtp",
                request,
                createAbhaService.updateMobileVerifyOtp(request).map(ResponseEntity::ok)
        );
    }


    // -------------------- Update Suggestion --------------------

    @Operation(summary = "Update ABHA Address", description = "This API sends an OTP to the new ABHA address to initiate address update.")
    @PostMapping("/update-abha-address")
    public Mono<ResponseEntity<ApiResponse<AbdmUpdateAddressResponse>>> updateSuggestion(@Valid @RequestBody AbhaSuggestionUpdateRequest abhaSuggestionUpdateRequest) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/update-abha-address",
                abhaSuggestionUpdateRequest,
                createAbhaService.updateSuggestion(abhaSuggestionUpdateRequest).map(ResponseEntity::ok)
        );
    }


    // -------------------- Update Profile Photo --------------------

    @Operation(summary = "Profile photo update", description = "This API update the user profile.")
    @PostMapping("/update-profile-photo")
    public Mono<ResponseEntity<ApiResponse<AbhaProfileResponse>>> updateProfilePhoto(@Valid @RequestBody UpdateProfileRequest updateProfileRequest) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/create/update-profile-photo",
                updateProfileRequest,
                createAbhaService.updateProfilePhoto(updateProfileRequest).map(ResponseEntity::ok)
        );
    }


}
