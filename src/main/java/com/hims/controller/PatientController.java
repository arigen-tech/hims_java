package com.hims.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasAppointmentChangeReason;
import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.entity.Visit;
import com.hims.entity.repository.PatientRepository;
import com.hims.projection.PatientProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.MasAppointmentChangeReasonService;
import com.hims.service.OpdPatientDetailService;
import com.hims.service.PatientService;
import com.hims.utils.ResponseUtils;
import com.hims.utils.StockFound;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "PatientController", description = "This controller is used for any Patient Related task.")
@RequestMapping("/patient")
@Slf4j
public class PatientController {
    @Autowired
    PatientService patientService;
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StockFound stockFound;

    @Autowired
    private OpdPatientDetailService opdPatientDetailService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;



    @GetMapping("/getOpdByVisit")
    public ResponseEntity<ApiResponse<OpdPatientVitalResponce>> getOpdPatientByVisit(
            @RequestParam Long visitId) {

        ApiResponse<OpdPatientVitalResponce> response = opdPatientDetailService.getOpdPatientByVisit(visitId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PatientRegFollowUpResp>> registerPatient(@RequestBody PatientRegistrationReq request) {
        ApiResponse<PatientRegFollowUpResp> response = patientService.registerPatientWithOpd(request.getPatient(), request.getOpdPatientDetail(), request.getVisits());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<PatientRegFollowUpResp>> updatePatient(@RequestBody PatientFollowUpReq request) {
        ApiResponse<PatientRegFollowUpResp> response = patientService.updatePatient(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("bookAppointment/{patientId}")
    public ResponseEntity<ApiResponse<BookingAppointmentResponse>> bookAppointment(@PathVariable Long patientId , @RequestBody VisitRequest visitRequest){
        ApiResponse<BookingAppointmentResponse> response = patientService.bookAppointment(patientId, visitRequest);
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            ApiResponse response = patientService.uploadImage(file);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(ResponseUtils.createFailureResponse(e.getMessage(), new TypeReference<String>() {
            },"Error uploading image",500), HttpStatus.OK);
        }
    }
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<PatientProjection>>> searchPatient(@RequestBody PatientSearchReq searchRequest){
        ApiResponse<List<PatientProjection>> response = patientService.searchPatient(searchRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/getPendingPreConsultations")
    public ResponseEntity<ApiResponse<List<Visit>>> getPendingPreConsultations(){
        ApiResponse<List<Visit>> response = patientService.getPendingPreConsultations();
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    @PostMapping("/saveVitalDetails")
    public ResponseEntity<ApiResponse<String>> saveVitalDetails(@RequestBody OpdPatientDetailRequest request){
        ApiResponse<String> response=patientService.saveVitalDetails(request);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicatePatient(
            @RequestParam String firstName,
            @RequestParam String dob,
            @RequestParam Long gender,
            @RequestParam String mobile,
            @RequestParam Long relation) {
        boolean exists = patientRepository.existsByPatientFnAndPatientDobAndPatientGenderIdAndPatientMobileNumberAndPatientRelationId(
                firstName.trim(),
                LocalDate.parse(dob),
                gender,
                mobile.trim(),
                relation);
        return ResponseEntity.ok(exists);
    }



    @GetMapping("/getWaitingList")
    public ResponseEntity<ApiResponse<List<Visit>>> getWaitingList(){
        ApiResponse<List<Visit>> response = patientService.getWaitingList();
        return new ResponseEntity<>(response,HttpStatus.OK);
    }



    @PostMapping("/patient-details")
    public ResponseEntity<ApiResponse<OpdPatientDetail>> createOpdPatientDetail(
            @Valid @RequestBody OpdPatientDetailFinalRequest request) {

        ApiResponse<OpdPatientDetail> response = opdPatientDetailService.createOpdPatientDetail(request);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/update-recall-patient")
    public ResponseEntity<ApiResponse<OpdPatientDetail>> updateRecallOpdPatient(
            @Valid @RequestBody RecallOpdPatientDetailRequest request) {

        ApiResponse<OpdPatientDetail> response = opdPatientDetailService.recallOpdPatientDetail(request);

        return ResponseEntity.ok(response);
    }


//    @GetMapping("/activeVisit")
//    public ResponseEntity<ApiResponse<List<OpdPatientDetailsWaitingresponce>>> getActiveVisits() {
//        ApiResponse<List<OpdPatientDetailsWaitingresponce>> response = opdPatientDetailService.getActiveVisits();
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/activeVisit/search")
    public ResponseEntity<ApiResponse<List<OpdPatientDetailsWaitingresponce>>> searchActiveVisits(
            @RequestBody ActiveVisitSearchRequest request
    ) {
        return ResponseEntity.ok(opdPatientDetailService.getActiveVisitsWithFilters(request));
    }



    @GetMapping("/recallVisit")
    public ResponseEntity<ApiResponse<List<OpdPatientRecallResponce>>> getRecallVisits(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate
    ) {
        ApiResponse<List<OpdPatientRecallResponce>> response =
                opdPatientDetailService.getRecallVisit(name, mobile, visitDate);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/changeStatusForClose/{visitId}/{status}")
    public ApiResponse<String> updateStatusForClose(
            @PathVariable Long visitId,
            @PathVariable String status) {
        return opdPatientDetailService.updateVisitStatus(visitId, status);
    }


    @PutMapping("/update-status")
    public ResponseEntity<?> updateVisitStatus(
            @RequestParam Long visitId,
            @RequestParam Instant visitDate,
            @RequestParam Long doctorId) {

        Visit updatedVisit = opdPatientDetailService.updateVisitStatus(
                visitId, visitDate, doctorId
        );

        messagingTemplate.convertAndSend("/topic/statusUpdated", "updated");

        return ResponseEntity.ok(updatedVisit);
    }


    @GetMapping("/available")
    public ResponseEntity<?> getAvailableStock(
            @RequestParam Long hospitalId,
            @RequestParam Integer departmentId,
            @RequestParam Long itemId,
            @RequestParam Integer noOfDays
    ) {

        Long availableStock = stockFound.getAvailableStocks(hospitalId, departmentId, itemId, noOfDays);

        if (availableStock == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Stock not found or invalid inputs.");
        }

        return ResponseEntity.ok(availableStock);
    }
    @PostMapping("/updatepaymentstatus")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentStatusResponse(@RequestBody PaymentUpdateRequest request) {
        return new ResponseEntity<>(patientService.paymentStatusReq(request), HttpStatus.OK);
    }

    @GetMapping("/getFullDetails/{patientId}")
    public ResponseEntity<ApiResponse<FollowUpPatientResponseDetails>> getPatientFullDetails(@PathVariable Long patientId) {
        ApiResponse<FollowUpPatientResponseDetails> response = patientService.getAllFollowUpDetails(patientId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/cancel_appointment")
    public ResponseEntity<?> cancelAppointment(@RequestBody CancelAppointmentRequest request) {
        ApiResponse<String> response = patientService.cancelAppointment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/reschedule_Appointment")
    public ResponseEntity<ApiResponse<RescheduleAppointmentResponse>> rescheduleAppointment(@RequestBody RescheduleAppointmentRequest request){
        ApiResponse<RescheduleAppointmentResponse> response = patientService.rescheduleAppointment(request);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    /**
     * Fetches cancelled appointments based on filters
     *
     * @param hospitalId Hospital ID (required)
     * @param departmentId Department ID (optional)
     * @param doctorId Doctor ID (optional)
     * @param fromDate From date in format yyyy-MM-dd (optional)
     * @param toDate To date in format yyyy-MM-dd (optional)
     * @param cancellationReasonId Cancellation reason ID (optional)
     * @return List of cancelled appointments with patient details
     */
    @GetMapping("/cancelledAppointments")
    public ResponseEntity<ApiResponse<List<CancelledAppointmentResponse>>> getCancelledAppointments(
            @RequestParam(required = true) Long hospitalId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long cancellationReasonId
    ) {
        log.info("GET /patient/cancelled-appointments called: hospitalId={}, departmentId={}, doctorId={}, fromDate={}, toDate={}, cancellationReasonId={}",
                hospitalId, departmentId, doctorId, fromDate, toDate, cancellationReasonId);

        ApiResponse<List<CancelledAppointmentResponse>> response =
                patientService.getCancelledAppointments(
                        hospitalId, departmentId, doctorId, fromDate, toDate, cancellationReasonId
                );

        return ResponseEntity.ok(response);
    }

}
