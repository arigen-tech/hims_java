package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.request.LabRadioRegistrationRequest;
import com.hims.request.LabRadioUpdateRequest;
import com.hims.request.RadRegInvReq;
import com.hims.request.RadiologyReportRequest;
import com.hims.response.*;
import com.hims.service.RadiologyService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "PatientController", description = "This controller is used for any Patient Related task.")
    @RequestMapping("/radiology")
@Slf4j
public class RadiologyController {

    @Autowired
    RadiologyService radiologyService;

    @PostMapping("/registerWithInv")
    public ResponseEntity<ApiResponse<LabRadiologyRegistrationResponse>> registerPatient(@RequestBody RadRegInvReq request) {
        ApiResponse<LabRadiologyRegistrationResponse> response = radiologyService.registerPatientWithInv(request.getPatient(), request.getRadInvestigationReq());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/radiologyRegistration")
    public ResponseEntity<ApiResponse<LabRadiologyRegistrationResponse>> registerAndBookingRadiology(
            @Valid @RequestBody LabRadioRegistrationRequest request) {
        try {
            log.info("Radiology Registration API called for patient: {} {}", 
                request.getPatient().getPatientFn(), request.getPatient().getPatientLn());
            
            ApiResponse<LabRadiologyRegistrationResponse> response = 
                radiologyService.registerAndBookingRadiology(
                    request.getPatient(), 
                    request.getInvestigationReq()
                );
            
            Integer statusCode = response.getStatus();
            if (statusCode != null && statusCode == 200) {
                log.info("Radiology Registration successful for patient ID: {}", 
                    response.getResponse().getPatientId());
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else if (statusCode != null && statusCode == 409) {
                log.warn("Patient already registered: {}", response.getMessage());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else if (statusCode != null && statusCode == 400) {
                log.warn("Radiology Registration invalid input: {}", response.getMessage());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else {
                log.warn("Radiology Registration failed: {}", response.getMessage());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Unexpected error in Radiology Registration", e);
            ApiResponse<LabRadiologyRegistrationResponse> errorResponse = 
                ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, 
                    "Internal Server Error", 500);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateDetailsAndBookingRadiology")
    public ResponseEntity<LabRadioUpdateResponse> updateDetailsAndBooking(
            @Valid @RequestBody LabRadioUpdateRequest request) {
        
        try {
            log.info("Patient Details Update and Radiology Booking API called for patient: {} {}", 
                request.getPatient().getPatientFn(), request.getPatient().getPatientLn());

            LabRadioUpdateResponse response = radiologyService.updatePatientDetailsAndBooking(request);

            if (response != null && response.getBillingHeaderIds() != null && !response.getBillingHeaderIds().isEmpty()) {
                log.info("Patient update and radiology booking successful. Total billings: {}", 
                    response.getBillingHeaderIds().size());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (response != null && "Patient updated successfully".equals(response.getMessage())) {
                log.info("Patient updated successfully without investigations");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                log.warn("Patient update failed: {}", response != null ? response.getMessage() : "Unknown error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Unexpected error in Patient Details Update and Radiology Booking", e);
            LabRadioUpdateResponse errorResponse = new LabRadioUpdateResponse(null, null, 
                "Internal Server Error: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pendingInvestigationForRadiology")
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPendingRadiology(
            @RequestParam(required = true) Long modality,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return radiologyService.getPendingRadiology(modality, patientName, phoneNumber, page, size);
    }
    //  status use cancel=c and complete=y
    @PutMapping("/cancelOrCompleteInvestigationRadiology")
    public ApiResponse<String> cancelOrCompleteInvestigationRadiology(@RequestParam Long id,@RequestParam String status) {
        return radiologyService.cancelOrCompleteInvestigationRadiology(id,status);
    }
    @GetMapping("/pendingListForRadiologyReport")
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPendingListForRadiologyReport(
            @RequestParam(required = false) Long modality,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return radiologyService.getPendingListForRadiologyReport(modality, patientName, phoneNumber, page, size);
    }
    @PostMapping("/saveDetailsReportForRadiology")
    public ResponseEntity<ApiResponse<String>> saveDetailsReportForRadiology(
            @Valid @RequestBody RadiologyReportRequest request,
            @RequestParam String status) {
        ApiResponse<String> response = radiologyService.saveDetailsReportForRadiology(request, status);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getPACSStudyList")
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPACSStudyList(
            @RequestParam(required = false) Long modality,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return radiologyService.getPACSStudyList(modality, patientName, phoneNumber, page, size);
    }


}

