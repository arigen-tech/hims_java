package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.PatientLogin;
import com.hims.entity.repository.PatientLoginRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.jwt.JwtHelper;
import com.hims.mapper.PatientLoginMapper;
import com.hims.projection.PatientVisitStatusCountProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.*;
import com.hims.utils.ResponseUtils;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileServiceImpl implements MobileService {
    private final MobileLoginService mobileLoginService;
    private final PatientPasswordService patientPasswordService;
    private final MasEmployeeService masEmployeeService;
    private final BillingService billingService;
    private final AuthService authService;
    private final PatientLoginRepository patientLoginRepository;
    private final PatientRepository patientRepository;
    private final JwtHelper jwtHelper;
    private final PatientLoginMapper patientLoginMapper;

    @Value("${sms.otp.base-url}${sms.otp.verify-path}")
    private String otpVerifyUrl;

    @Override public ApiResponse<MobileLoginResponce> login(LoginRequest request) {
        return mobileLoginService.loginRequest(request);
    }
    @Override public ApiResponse<AuthResponse> passwordLogin(PatientPasswordLoginRequest request) {
        return patientPasswordService.login(request);
    }
    @Override public ApiResponse<MobileLoginResponce> forgotPassword(ForgotPatientPasswordRequest request) {
        return patientPasswordService.requestPasswordReset(request);
    }
    @Override public ApiResponse<String> resetPassword(ResetPatientPasswordRequest request) {
        return patientPasswordService.resetPassword(request);
    }
    @Override public ApiResponse<AuthResponse> switchPatient(Long patientId, String mobileNumber) {
        return mobileLoginService.switchPatient(patientId, mobileNumber);
    }
    @Override public ApiResponse<String> deactivatePatient(Long patientId) {
        return mobileLoginService.deactivatePatient(patientId);
    }
    @Override public ApiResponse<MobileLoginResponce> sendPatientActivationOtp(PatientActivationOtpRequest request) {
        return mobileLoginService.sendPatientActivationOtp(request);
    }
    @Override public ApiResponse<String> verifyPatientActivationOtp(PatientActivationOtpRequest request) {
        return mobileLoginService.verifyPatientActivationOtp(request);
    }

    @Override
    public ApiResponse<List<PatientIdResponse>> getPatientsForActivation(String mobileNo) {
        List<PatientLogin> inactiveLogins = patientLoginRepository
                .findByMobileNoOrderByPatientLoginIdDesc(mobileNo).stream()
                .filter(login -> !isActive(login))
                .collect(Collectors.toMap(PatientLogin::getPatientId, login -> login,
                        (first, ignored) -> first, java.util.LinkedHashMap::new))
                .values().stream().toList();
        if (inactiveLogins.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "No inactive patients found for this mobile number", HttpStatus.NOT_FOUND.value());
        }
        List<PatientIdResponse> patients = inactiveLogins.stream()
                .map(login -> patientRepository.findById(login.getPatientId())
                        .map(patient -> patientLoginMapper.toPatientIdResponse(patient, login)).orElse(null))
                .filter(Objects::nonNull).toList();
        return ResponseUtils.createSuccessResponse(patients, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<?> verifyOtp(OtpRequest request) {
        try {
            List<PatientLogin> allLogins = patientLoginRepository
                    .findByMobileNoOrderByPatientLoginIdDesc(request.getMobileNo());
            List<PatientLogin> activeLogins = allLogins.stream().filter(this::isActive).toList();
            if (activeLogins.isEmpty()) {
                if (!allLogins.isEmpty()) {
                    return failure("Patient is Currently Inactive", HttpStatus.FORBIDDEN);
                }
                return failure("No active patient is linked to this mobile number", HttpStatus.UNAUTHORIZED);
            }

            String uri = otpVerifyUrl
                    .replace("{sessionId}", request.getSessionId())
                    .replace("{otp}", request.getOtp());
            HttpResponse<String> response = Unirest.post(uri)
                    .header("content-type", "application/x-www-form-urlencoded").asString();
            if (!"Success".equalsIgnoreCase(new JSONObject(response.getBody()).optString("Status"))) {
                return failure("Invalid OTP", HttpStatus.BAD_REQUEST);
            }

            Long patientId = activeLogins.get(0).getPatientId();
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(jwtHelper.mobileGenerateToken(request.getMobileNo(), patientId));
            authResponse.setRefreshToken(jwtHelper.mobileGenerateRefreshToken(request.getMobileNo(), patientId));
            authResponse.setPatientIdResponseList(allLogins.stream()
                    .collect(Collectors.toMap(PatientLogin::getPatientId, login -> login,
                            (first, ignored) -> first, java.util.LinkedHashMap::new))
                    .values().stream()
                    .map(login -> patientRepository.findById(login.getPatientId())
                            .map(patient -> patientLoginMapper.toPatientIdResponse(patient, login)).orElse(null))
                    .filter(Objects::nonNull).toList());
            authResponse.setMessage("OTP verified successfully");
            return ResponseUtils.createSuccessResponse(authResponse, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Unable to verify mobile OTP for {}", request.getMobileNo(), e);
            return failure("Unable to verify OTP Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isActive(PatientLogin login) {
        return login.getStatus() == null || !"N".equalsIgnoreCase(login.getStatus());
    }

    private <T> ApiResponse<T> failure(String message, HttpStatus status) {
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, message, status.value());
    }

    @Override public ApiResponse<?> refreshToken(RefreshTokenRequest request) { return authService.refreshToken(request); }
    @Override public ApiResponse<List<SpecialitiesAndDoctorResponse>> searchBySpecialityAndDoctor(String search, Long hospitalId) {
        return masEmployeeService.getDepartmentAndDoctor(search, hospitalId);
    }
    @Override public ApiResponse<List<SpecialityResponse>> getAllDoctorBySpecialityWise(Long specialityId) {
        return masEmployeeService.getSpecialityAndDoctor(specialityId);
    }
    @Override public ApiResponse<DoctorDetailResponse> getDoctorDetailById(Long doctorId) { return masEmployeeService.getDoctor(doctorId); }
    @Override public ApiResponse<List<AppointmentBookingHistoryResponseDetails>> getAppointmentHistoryList(
            Long hospitalId, String patientName, String name, Long patientId, String mobileNo,
            String deptTypeCode, Boolean includeAllHistory, String payment, String visitStatus) {
        String resolvedPatientName = patientName != null ? patientName : name;
        return masEmployeeService.appointmentHistoryList(hospitalId, patientId, mobileNo, resolvedPatientName,
                deptTypeCode, includeAllHistory, payment, visitStatus);
    }
    @Override public ApiResponse<PatientVisitStatusCountProjection> getPatientVisitStatusCounts(Long hospitalId, Long patientId) {
        return masEmployeeService.getPatientVisitStatusCounts(hospitalId, patientId);
    }
    @Override public ApiResponse<Page<MobileCancelledRefundResponse>> getCancelledRefundAppointments(
            Long hospitalId, String departmentType, Long patientId, int page, int size) {
        return billingService.getMobileCancelledRefundAppointments(hospitalId, departmentType, patientId, page, size);
    }
}
