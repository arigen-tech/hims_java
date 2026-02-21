package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.repository.PatientRepository;
import com.hims.projection.PatientProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.RegistrationService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for patient registration and appointment management
 */
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;

    private final PatientRepository patientRepository;

    /**
     * Register new patient with OPD details
     */
    @PostMapping("/createPatient")
    public ResponseEntity<ApiResponse<PatientRegFollowUpResp>> createPatient(
            @RequestBody PatientRegistrationReq request) {
        log.info("POST /registration/createPatient called");
        ApiResponse<PatientRegFollowUpResp> response = registrationService.createPatient(
                request.getPatient(), request.getOpdPatientDetail(), request.getVisits());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update existing patient details
     */
    @PostMapping("/updatePatient")
    public ResponseEntity<ApiResponse<PatientRegFollowUpResp>> updatePatient(
            @RequestBody PatientFollowUpReq request) {
        log.info("POST /registration/updatePatient called");
        ApiResponse<PatientRegFollowUpResp> response = registrationService.updatePatient(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Book appointment for existing patient
     */
    @PostMapping("/bookAppointment/{patientId}")
    public ResponseEntity<ApiResponse<BookingAppointmentResponse>> bookAppointment(
            @PathVariable @Parameter(description = "Patient ID") Long patientId,
            @RequestBody VisitRequest visitRequest) {
        log.info("POST /registration/bookAppointment/{} called", patientId);
        ApiResponse<BookingAppointmentResponse> response = registrationService.bookAppointment(patientId, visitRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Upload patient image
     */
    @PostMapping("/uploadPatientImage")
    public ResponseEntity<ApiResponse<String>> uploadPatientImage(
            @RequestParam("file") MultipartFile file) {
        log.info("POST /registration/uploadPatientImage called");
        try {
            ApiResponse<String> response = registrationService.uploadPatientImage(file);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(ResponseUtils.createFailureResponse(e.getMessage(),
                    new TypeReference<>() {}, "Error uploading image", 500), HttpStatus.OK);
        }
    }

    /**
     * Search patients by mobile number and name
     */
    @PostMapping("/searchPatient")
    public ResponseEntity<ApiResponse<List<PatientProjection>>> searchPatient(
            @RequestBody PatientSearchReq searchRequest) {
        log.info("POST /registration/searchPatient called");
        ApiResponse<List<PatientProjection>> response = registrationService.searchPatient(searchRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Check for duplicate patient
     */
    @GetMapping("/checkDuplicatePatient")
    public ResponseEntity<Boolean> checkDuplicatePatient(
            @RequestParam @Parameter(description = "First name") String firstName,
            @RequestParam @Parameter(description = "Date of birth in yyyy-MM-dd format") String dob,
            @RequestParam @Parameter(description = "Gender ID") Long gender,
            @RequestParam @Parameter(description = "Mobile number") String mobile,
            @RequestParam @Parameter(description = "Relation ID") Long relation) {
        log.info("GET /registration/checkDuplicatePatient called");
        boolean exists = patientRepository.existsByPatientFnAndPatientDobAndPatientGenderIdAndPatientMobileNumberAndPatientRelationId(
                firstName.trim(), LocalDate.parse(dob), gender, mobile.trim(), relation);
        return ResponseEntity.ok(exists);
    }

    /**
     * Get patient full details for follow-up
     */
    @GetMapping("/getPatientDetails/{patientId}")
    public ResponseEntity<ApiResponse<FollowUpPatientResponseDetails>> getPatientDetails(
            @PathVariable @Parameter(description = "Patient ID") Long patientId) {
        log.info("GET /registration/getPatientDetails/{} called", patientId);
        ApiResponse<FollowUpPatientResponseDetails> response = registrationService.getPatientDetails(patientId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update payment status
     */
    @PostMapping("/updatePaymentStatus")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @RequestBody PaymentUpdateRequest request) {
        log.info("POST /registration/updatePaymentStatus called");
        return new ResponseEntity<>(registrationService.updatePaymentStatus(request), HttpStatus.OK);
    }

    /**
     * Cancel appointment
     */
    @PostMapping("/cancelAppointment")
    public ResponseEntity<?> cancelAppointment(
            @RequestBody CancelAppointmentRequest request) {
        log.info("POST /registration/cancelAppointment called");
        ApiResponse<String> response = registrationService.cancelAppointment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Reschedule appointment
     */
    @PostMapping("/rescheduleAppointment")
    public ResponseEntity<ApiResponse<RescheduleAppointmentResponse>> rescheduleAppointment(
            @RequestBody RescheduleAppointmentRequest request) {
        log.info("POST /registration/rescheduleAppointment called");
        ApiResponse<RescheduleAppointmentResponse> response = registrationService.rescheduleAppointment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get cancelled appointments with filters
     */
    @GetMapping("/getCancelledAppointments")
    public ResponseEntity<ApiResponse<List<CancelledAppointmentResponse>>> getCancelledAppointments(
            @RequestParam @Parameter(description = "Hospital ID") Long hospitalId,
            @RequestParam(required = false) @Parameter(description = "Department ID (optional)") Long departmentId,
            @RequestParam(required = false) @Parameter(description = "Doctor ID (optional)") Long doctorId,
            @RequestParam(required = false) @Parameter(description = "From date in yyyy-MM-dd format (optional)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @Parameter(description = "To date in yyyy-MM-dd format (optional)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @Parameter(description = "Cancellation reason ID (optional)") Long cancellationReasonId) {
        log.info("GET /registration/getCancelledAppointments called: hospitalId={}, departmentId={}, doctorId={}, fromDate={}, toDate={}, cancellationReasonId={}",
                hospitalId, departmentId, doctorId, fromDate, toDate, cancellationReasonId);
        ApiResponse<List<CancelledAppointmentResponse>> response = registrationService.getCancelledAppointments(
                hospitalId, departmentId, doctorId, fromDate, toDate, cancellationReasonId);
        return ResponseEntity.ok(response);
    }
}


