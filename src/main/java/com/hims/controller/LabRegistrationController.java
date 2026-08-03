package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.projection.BillingHeaderResponseProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.*;
import com.hims.service.impl.BillingServiceImpl;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "LabRegistration", description = "This controller is used for any LabRegistration & Investigation & Package booking Related task.")
@RequestMapping("/lab")
@Slf4j
@RequiredArgsConstructor
public class LabRegistrationController {

    @Autowired
    LabRegistrationServices labRegistrationServices;

    @Autowired
    RadiologyService radiologyService;

    @Autowired
    SampleValidationService validationService;
    @Autowired
    ResultService resultService;

      // or your service interface
    @Autowired
    BillingService billingService;

    @Autowired
    private LabOrderTrackingStatusService labOrderTrackingStatusService;


    /**
     * Registers a new laboratory patient and books investigations or packages for them.
     * This endpoint handles new patient registrations with initial investigation/package bookings.
     * 
     * @param request The laboratory registration request containing patient details and investigations to be booked
     * @return ResponseEntity with HTTP status CREATED containing ApiResponse with LabRadiologyRegistrationResponse
     */
    @PostMapping("/laboratoryRegistration")
    public ResponseEntity<ApiResponse<LabRadiologyRegistrationResponse>> registerAndBookingLaboratory(
            @RequestBody @Valid LabRadioRegistrationRequest request) {
        log.info("Processing laboratory registration for request: {}", request);
        ApiResponse<LabRadiologyRegistrationResponse> response = labRegistrationServices.registerAndBookingLaboratory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates patient details for an already registered laboratory patient and books additional investigations.
     * This endpoint allows modification of existing patient information and adding new investigations/packages.
     * 
     * @param request The update request containing patient information updates and new investigations to book
     * @return ResponseEntity containing ApiResponse with AppsetupResponse data
     */
    @PostMapping("/updateDetailsAndBookingLaboratory")
    public ResponseEntity<ApiResponse<AppsetupResponse>> updateDetailsAndBookingLaboratory(@RequestBody LabRadioUpdateRequest request) {
        log.info("Lab Registration API called");
        return new ResponseEntity<>(labRegistrationServices.updateDetailsAndBookingLaboratory(request), HttpStatus.OK);
    }


    
    /**
     * Performs billing registration for an existing laboratory order.
     * Handles billing-only registration for orders that have already been created.
     * 
     * @param labReq The laboratory billing request containing order header ID and billing details
     * @return ApiResponse containing AppsetupResponse with billing confirmation
     */
    @PostMapping("/registration/billing")
    public ApiResponse<AppsetupResponse> labRegistrationForExistingOrder(
            @RequestBody LabBillingOnlyRequest labReq) {
        log.info("Received lab registration billing request for existing order. OrderHdId={}",
                labReq.getOrderhdid());
        return labRegistrationServices.labRegForExistingOrder(labReq);
    }


    /**
     * Creates a new lab order tracking status record.
     * Tracks the status and progression of laboratory orders through the system.
     *
     * @param request The lab order tracking status request containing tracking details
     * @return ResponseEntity with HTTP status CREATED containing the created tracking status response
     */
    @PostMapping("/track-order-status/create")
    public ResponseEntity<?> create(@Valid @RequestBody LabOrderTrackingStatusRequest request) {
        log.info("track-order-status/create api called");
        return ResponseEntity.status(HttpStatus.CREATED).body(labOrderTrackingStatusService.create(request));
    }

    /**
     * Retrieves all investigation results for a specific patient.
     * Fetches the investigation results history for a patient within a specific hospital.
     * 
     * @param patientId The unique identifier of the patient
     * @param hospitalId The unique identifier of the hospital
     * @return ApiResponse containing a list of ResultForInvestigationResponse objects with patient's investigation results
     */
    @GetMapping("/investigationResultByPatient")
    public ApiResponse<List<ResultForInvestigationResponse>> getResultForInvestigation(@RequestParam Long patientId,@RequestParam Long hospitalId) {
        log.info("investigationResultForMobile");
        return  resultService.getResultForInvestigation(patientId,hospitalId);
    }

//    @PostMapping("/saveResultEntry")
//    public ResponseEntity<ApiResponse<String>> saveOrUpdate(@RequestBody ResultEntryMainRequest request) {
//        log.info("Received saveOrUpdateResultEntry request for sampleCollectionHeaderId={}, subChargeCodeId={}",
//                request.getSampleCollectionHeaderId(), request.getSubChargeCodeId());
//        ApiResponse<String> response = resultService.saveOrUpdateResultEntry(request);
//        log.info("saveOrUpdateResultEntry response status={}", response.getStatus());
//        return ResponseEntity.ok(response);
//    }
    
}
