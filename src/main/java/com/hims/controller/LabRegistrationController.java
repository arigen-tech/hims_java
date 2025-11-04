package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.service.LabRegistrationServices;
import com.hims.service.ResultService;
import com.hims.service.SampleValidationService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    SampleValidationService validationService;
    @Autowired
    ResultService resultService;


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

    @GetMapping("/order-status")
    public ApiResponse<List<SampleValidationResponse>> getAllWithStatusNAndP2() {
        return validationService.getInvestigationsWithOrderStatusNAndP();
    }
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateInvestigations(@RequestBody List<InvestigationValidationRequest> requests) {
        ApiResponse<String> stringApiResponse = validationService.validateInvestigations(requests);
        return   ResponseEntity.ok(stringApiResponse);
    }
    @GetMapping("/resultStatus")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getValidated() {
        ApiResponse<List<ResultResponse>> responseList = validationService.getValidatedResultEntries();
        return ResponseEntity.ok(responseList);
    }
    @PostMapping("/saveResultEntry")
    public ResponseEntity<ApiResponse<String>> saveOrUpdate(@RequestBody ResultEntryMainRequest request) {
        ApiResponse<String> response = resultService.saveOrUpdateResultEntry(request);
        return ResponseEntity.ok(response);
    }
}
