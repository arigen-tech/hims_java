package com.hims.service;

import com.hims.projection.PatientProjection;
import com.hims.request.*;
import com.hims.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public interface RegistrationService {

    ApiResponse<PatientRegFollowUpResp> createPatient(PatientRequest patient, OpdPatientDetailRequest opdPatientDetail, List<VisitRequest> visit);

    ApiResponse<PatientRegFollowUpResp> updatePatient(PatientFollowUpReq request);

    ApiResponse<String> uploadPatientImage(MultipartFile file);

    ApiResponse<Page<PatientProjection>> searchPatient(PatientSearchReq substring, Pageable pageable);

    ApiResponse<FollowUpPatientResponseDetails> getPatientDetails(Long patient, String serviceCategoryCode);

    ApiResponse<PaymentResponse> updatePaymentStatus(PaymentUpdateRequest opdreq);

    ApiResponse<String> cancelAppointment(CancelAppointmentRequest request);

    ApiResponse<RescheduleAppointmentResponse> rescheduleAppointment(RescheduleAppointmentRequest request);

    ApiResponse<BookingAppointmentResponse> bookAppointment(Long patientId, VisitRequest visitRequest);

    ApiResponse<List<CancelledAppointmentResponse>> getCancelledAppointments(Long hospitalId, Long departmentId, Long doctorId, LocalDate fromDate, LocalDate toDate, Long cancellationReasonId);

    ApiResponse<List<AvailableTokenSlotResponse>> getAppointmentSlots(Long deptId, Long doctorId, String appointmentDate, Long sessionId, int flag);

    ApiResponse<List<?>> getAppointmentSummaryReport(Long hospitalId, Long departmentId, Long doctorId, LocalDate fromDate, LocalDate toDate,Integer flag);

    boolean checkDuplicatePatient(String firstName,  LocalDate dob, Long gender, String mobile, Long relation);

}



