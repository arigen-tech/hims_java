package com.hims.service;

import com.hims.projection.PatientVisitStatusCountProjection;
import com.hims.request.*;
import com.hims.response.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MobileService {
    ApiResponse<MobileLoginResponce> login(LoginRequest request);
    ApiResponse<AuthResponse> passwordLogin(PatientPasswordLoginRequest request);
    ApiResponse<MobileLoginResponce> forgotPassword(ForgotPatientPasswordRequest request);
    ApiResponse<String> resetPassword(ResetPatientPasswordRequest request);
    ApiResponse<AuthResponse> switchPatient(Long patientId, String mobileNumber);
    ApiResponse<String> deactivatePatient(Long patientId);
    ApiResponse<List<PatientIdResponse>> getPatientsForActivation(String mobileNo);
    ApiResponse<MobileLoginResponce> sendPatientActivationOtp(PatientActivationOtpRequest request);
    ApiResponse<String> verifyPatientActivationOtp(PatientActivationOtpRequest request);
    ApiResponse<?> verifyOtp(OtpRequest request);
    ApiResponse<?> refreshToken(RefreshTokenRequest request);
    ApiResponse<List<SpecialitiesAndDoctorResponse>> searchBySpecialityAndDoctor(String search, Long hospitalId);
    ApiResponse<List<SpecialityResponse>> getAllDoctorBySpecialityWise(Long specialityId);
    ApiResponse<DoctorDetailResponse> getDoctorDetailById(Long doctorId);
    ApiResponse<List<AppointmentBookingHistoryResponseDetails>> getAppointmentHistoryList(
            Long hospitalId, String patientName, String name, Long patientId, String mobileNo,
            String deptTypeCode, Boolean includeAllHistory, String payment, String visitStatus);
    ApiResponse<PatientVisitStatusCountProjection> getPatientVisitStatusCounts(Long hospitalId, Long patientId);
    ApiResponse<Page<MobileCancelledRefundResponse>> getCancelledRefundAppointments(
            Long hospitalId, String departmentType, Long patientId, int page, int size);
}
