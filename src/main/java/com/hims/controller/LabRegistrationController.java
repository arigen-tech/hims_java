package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.request.LabRegRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.request.SampleCollectionRequest;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.service.LabRegistrationServices;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "LabRegistration", description = "This controller is used for any LabRegistration & Investigation & Package booking Related task.")
@RequestMapping("/lab")
@Slf4j
public class LabRegistrationController {

    @Autowired
    LabRegistrationServices labRegistrationServices;

    @Autowired
    BillingService billingService;
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<AppsetupResponse>> appSetupResponse(@RequestBody LabRegRequest request) {
        return new ResponseEntity<>(labRegistrationServices.labReg(request), HttpStatus.OK);

    }

    @PostMapping("/updatepaymentstatus")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentStatusResponse(@RequestBody PaymentUpdateRequest request) {
        return new ResponseEntity<>(labRegistrationServices.paymentStatusReq(request), HttpStatus.OK);
    }
    @GetMapping("/pending")
    public ApiResponse<List<PendingBillingResponse>> getPendingBilling() {
        return billingService.getPendingBilling();
    }

    @GetMapping("/pending-samples")
    public ResponseEntity<ApiResponse<List<PendingSampleResponse>>> getPendingSamples() {
        try {
            List<PendingSampleResponse> data = labRegistrationServices.getPendingSamples();
            ApiResponse<List<PendingSampleResponse>> response = ResponseUtils.createSuccessResponse(data, new TypeReference<>() {});
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<List<PendingSampleResponse>> errorResponse = ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/savesamplecollection")
    public ResponseEntity<ApiResponse<AppsetupResponse>>samplecollectionResponse(@RequestBody SampleCollectionRequest request) {
        return new ResponseEntity<>(labRegistrationServices.savesample(request), HttpStatus.OK);
    }



}
