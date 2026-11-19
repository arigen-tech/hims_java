package com.hims.controller;

import com.hims.projection.PatientVisitStatusCountProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.MobileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MobileController", description = "This controller is used for any ")
@RequestMapping("/mobileController")
@RequiredArgsConstructor
public class MobileController {
    private final MobileService mobileService;

    @PostMapping("/mLogin")
    public ResponseEntity<ApiResponse<MobileLoginResponce>> loginResponse(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(mobileService.login(request));
    }

    @PostMapping("/patientPasswordLogin")
    public ApiResponse<AuthResponse> patientPasswordLogin(@RequestBody @Valid PatientPasswordLoginRequest request) {
        return mobileService.passwordLogin(request);
    }

    @PostMapping("/forgotPatientPassword")
    public ApiResponse<MobileLoginResponce> forgotPatientPassword(@RequestBody @Valid ForgotPatientPasswordRequest request) {
        return mobileService.forgotPassword(request);
    }

    @PostMapping("/resetPatientPassword")
    public ApiResponse<String> resetPatientPassword(@RequestBody @Valid ResetPatientPasswordRequest request) {
        return mobileService.resetPassword(request);
    }

    @PostMapping("/switchPatient")
    public ApiResponse<AuthResponse> switchPatient(@RequestParam Long patientId, @RequestParam String mobileNumber) {
        return mobileService.switchPatient(patientId, mobileNumber);
    }

    @PutMapping("/patientInactive/{patientId}")
    public ApiResponse<String> patientInactive(@PathVariable Long patientId) {
        return mobileService.deactivatePatient(patientId);
    }

    @GetMapping("/patientsForActivation")
    public ApiResponse<List<PatientIdResponse>> getPatientsForActivation(@RequestParam String mobileNo) {
        return mobileService.getPatientsForActivation(mobileNo);
    }

    @PostMapping("/sendPatientActivationOtp")
    public ApiResponse<MobileLoginResponce> sendPatientActivationOtp(
            @RequestBody @Valid PatientActivationOtpRequest request) {
        return mobileService.sendPatientActivationOtp(request);
    }

    @PostMapping("/verifyPatientActivationOtp")
    public ApiResponse<String> verifyPatientActivationOtp(@RequestBody @Valid PatientActivationOtpRequest request) {
        return mobileService.verifyPatientActivationOtp(request);
    }

    @PostMapping("/verifyOtp")
    public ApiResponse<?> verifyOtp(@RequestBody @Valid OtpRequest request) {
        return mobileService.verifyOtp(request);
    }

    @PostMapping("/refreshToken")
    public ApiResponse<?> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return mobileService.refreshToken(request);
    }

    @GetMapping("/searchBySpecialityAndDoctor")
    public ApiResponse<List<SpecialitiesAndDoctorResponse>> searchBySpecialityAndDoctor(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long hospitalId) {
        return mobileService.searchBySpecialityAndDoctor(search, hospitalId);
    }

    @GetMapping("/getAllDoctorBySpecialityWise")
    public ApiResponse<List<SpecialityResponse>> getAllDoctorBySpecialityWise(
            @RequestParam(required = false) Long specialityId) {
        return mobileService.getAllDoctorBySpecialityWise(specialityId);
    }

    @GetMapping("/getDoctorDetailById")
    public ApiResponse<DoctorDetailResponse> getDoctorDetailById(@RequestParam(required = false) Long doctorId) {
        return mobileService.getDoctorDetailById(doctorId);
    }

    @GetMapping("/getAppointmentHistoryList")
    public ApiResponse<List<AppointmentBookingHistoryResponseDetails>> getAppointmentHistoryList(
            @RequestParam Long hospitalId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String deptTypeCode,
            @RequestParam(required = false, defaultValue = "true") Boolean includeAllHistory,
            @RequestParam(required = false) String payment,
            @RequestParam(required = false) String visitStatus) {
        return mobileService.getAppointmentHistoryList(hospitalId, patientName, name, patientId, mobileNo,
                deptTypeCode, includeAllHistory, payment, visitStatus);
    }

    @GetMapping("/getPatientVisitStatusCounts")
    public ApiResponse<PatientVisitStatusCountProjection> getPatientVisitStatusCounts(
            @RequestParam Long hospitalId, @RequestParam Long patientId) {
        return mobileService.getPatientVisitStatusCounts(hospitalId, patientId);
    }

    @GetMapping("/getCancelledRefundAppointments")
    public ApiResponse<Page<MobileCancelledRefundResponse>> getCancelledRefundAppointments(
            @RequestParam Long hospitalId,
            @RequestParam String departmentType,
            @RequestParam Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return mobileService.getCancelledRefundAppointments(hospitalId, departmentType, patientId, page, size);
    }
}
