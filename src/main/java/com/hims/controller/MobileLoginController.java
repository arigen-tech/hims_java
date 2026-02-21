package com.hims.controller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.jwt.JwtHelper;
import com.hims.request.LoginRequest;
import com.hims.request.OtpRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AuthResponse;
import com.hims.response.MobileLoginResponce;
import com.hims.service.MobileLoginService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@Tag(name = "MobileLogin", description = "This controller is used for any ")
@RequestMapping("/mobilelogin")
@Slf4j
@RequiredArgsConstructor
public class MobileLoginController {
    @Autowired
    MobileLoginService mobileLoginService;
    @Autowired
    private JwtHelper jwtUtil;
    @PostMapping("/mLogin")
    public ResponseEntity<ApiResponse>  loginResponse(@RequestBody LoginRequest request) {
        return new ResponseEntity<>(mobileLoginService.loginRequest(request), HttpStatus.OK);
    }
    @PostMapping("/verifyOtp")
    public ApiResponse<?>  verifyOtp(@RequestBody @Valid OtpRequest otpRequest) {
        try {
            String otp = otpRequest.getOtp();
            String sessionId = otpRequest.getSessionId();

            String uri = "https://2factor.in/API/V1/999a2629-21e1-11ec-a13b-0200cd936042/SMS/VERIFY/"
                    + sessionId + "/" + otp;

            HttpResponse<String> response = Unirest.post(uri)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .asString();

            JSONObject json = new JSONObject(response.getBody());
            String status = json.getString("Status");

            //  Invalid OTP
            if (!"Success".equalsIgnoreCase(status)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid OTP", 400);
            }

            // OTP verified generate JWT
            String token = jwtUtil.mobileGenerateToken(
                    otpRequest.getMobileNo()
            );

            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setMessage("OTP verified successfully");
            return ResponseUtils.createSuccessResponse( authResponse, new TypeReference<>() {});

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Unable to verify OTP Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
  }
    private ResponseEntity<ApiResponse> createErrorResponse(String message) {
        return ResponseEntity.badRequest().body(new ApiResponse("error", message, null));
    }



}
