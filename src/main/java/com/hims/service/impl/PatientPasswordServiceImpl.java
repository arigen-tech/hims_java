package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.SMSTemplate;
import com.hims.entity.Patient;
import com.hims.entity.PatientLogin;
import com.hims.entity.repository.PatientLoginRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.jwt.JwtHelper;
import com.hims.mapper.PatientLoginMapper;
import com.hims.request.ForgotPatientPasswordRequest;
import com.hims.request.PatientPasswordLoginRequest;
import com.hims.request.ResetPatientPasswordRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AuthResponse;
import com.hims.response.MobileLoginResponce;
import com.hims.service.PatientPasswordService;
import com.hims.utils.ResponseUtils;
import com.hims.utils.SMSUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientPasswordServiceImpl implements PatientPasswordService {
    private final PatientLoginRepository patientLoginRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;
    private final SMSUtility smsUtility;
    private final PatientLoginMapper patientLoginMapper;

    @Override
    @Transactional
    public ApiResponse<AuthResponse> login(PatientPasswordLoginRequest request) {
        List<PatientLogin> logins = patientLoginRepository
                .findByMobileNoOrderByPatientLoginIdDesc(request.getMobileNo());
        if (logins.isEmpty()) {
            return failure("Invalid mobile number or password", HttpStatus.UNAUTHORIZED);
        }

        Optional<PatientLogin> matched = logins.stream()
                .filter(login -> login.getPassword() != null
                        && passwordEncoder.matches(request.getPassword(), login.getPassword()))
                .findFirst();
        if (matched.isEmpty()) {
            return failure("Invalid mobile number or password", HttpStatus.UNAUTHORIZED);
        }

        PatientLogin login = matched.get();
        Patient patient = patientRepository.findById(login.getPatientId()).orElse(null);
        if (patient == null) {
            return failure("Patient not found", HttpStatus.NOT_FOUND);
        }
        login.setLastLogin(Instant.now());

        AuthResponse response = new AuthResponse();
        response.setToken(jwtHelper.mobileGenerateToken(request.getMobileNo(), login.getPatientId()));
        response.setRefreshToken(jwtHelper.mobileGenerateRefreshToken(request.getMobileNo(), login.getPatientId()));
        response.setPatientIdResponseList(logins.stream()
                .map(PatientLogin::getPatientId)
                .distinct()
                .map(id -> patientRepository.findById(id).orElse(null))
                .filter(p -> p != null)
                .map(patientLoginMapper::toPatientIdResponse)
                .toList());
        response.setMessage("Login successful");
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<MobileLoginResponce> requestPasswordReset(ForgotPatientPasswordRequest request) {
        if (patientLoginRepository.findByMobileNoOrderByPatientLoginIdDesc(request.getMobileNo()).isEmpty()) {
            // Same response prevents enumeration of registered mobile numbers.
            MobileLoginResponce response = new MobileLoginResponce();
            response.setMobileNo(request.getMobileNo());
            response.setMessage("If this mobile number is registered, an OTP has been sent");
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        }
        try {
            String sessionId = smsUtility.sendOtp(request.getMobileNo(), SMSTemplate.LOGIN_OTP);
            MobileLoginResponce response = new MobileLoginResponce();
            response.setMobileNo(request.getMobileNo());
            response.setSessionId(sessionId);
            response.setMessage("OTP sent successfully");
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (RuntimeException e) {
            log.error("Unable to send patient password reset OTP", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Unable to send OTP. Please try again.", HttpStatus.BAD_GATEWAY.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> resetPassword(ResetPatientPasswordRequest request) {
        List<PatientLogin> logins = patientLoginRepository
                .findByMobileNoOrderByPatientLoginIdDesc(request.getMobileNo());
        if (logins.isEmpty()) {
            return failure("Invalid mobile number or OTP", HttpStatus.BAD_REQUEST);
        }
        if (!smsUtility.verifyOtp(request.getSessionId(), request.getOtp())) {
            return failure("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        logins.forEach(login -> login.setPassword(encodedPassword));
        patientLoginRepository.saveAll(logins);
        return ResponseUtils.createSuccessResponse("Password updated successfully", new TypeReference<>() {});
    }

    private <T> ApiResponse<T> failure(String message, HttpStatus status) {
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, message, status.value());
    }
}
