package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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

      // or your service interface
    @Autowired
    BillingService billingService;

    @Autowired
    private LabOrderTrackingStatusService labOrderTrackingStatusService;


    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<AppsetupResponse>> appSetupResponse(@RequestBody LabRegRequest request) {
        log.info("Lab Registration API called");
        return new ResponseEntity<>(labRegistrationServices.labReg(request), HttpStatus.OK);

    }
    @PostMapping("/updatepaymentstatus")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentStatusResponse(@RequestBody PaymentUpdateRequest request) {
        log.info("Update Payment Status API called");
        return new ResponseEntity<>(labRegistrationServices.paymentStatusReq(request), HttpStatus.OK);
    }
    @GetMapping("/pending")
    public ApiResponse<List<PendingBillingResponse>> getPendingBilling() {
        log.info("Get Pending Billing API called");
        return billingService.getPendingBilling();
    }

    @GetMapping("/pending-samples")
    public ResponseEntity<ApiResponse<List<PendingSampleResponse>>> getPendingSamples() {
        log.info("Get Pending Samples API called");
        try {
            List<PendingSampleResponse> data = labRegistrationServices.getPendingSamples();
            log.info("Pending samples fetched successfully, count={}",
                    data != null ? data.size() : 0);
            ApiResponse<List<PendingSampleResponse>> response = ResponseUtils.createSuccessResponse(data, new TypeReference<>() {});
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.warn("Validation error in getPendingSamples: {}", ex.getMessage());
            ex.printStackTrace();

            ApiResponse<List<PendingSampleResponse>> errorResponse = ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/savesamplecollection")
    public ResponseEntity<ApiResponse<AppsetupResponse>> samplecollectionResponse(@RequestBody SampleCollectionRequest request) {
        log.info("Get Sample Collection Response API called");
        return new ResponseEntity<>(labRegistrationServices.savesample(request), HttpStatus.OK);
    }

    @GetMapping("/order-status")
    public ApiResponse<List<SampleValidationResponse>> getAllWithStatusNAndP2() {
        log.info("order-status API called");
        return validationService.getInvestigationsWithOrderStatusNAndP();
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateInvestigations(@RequestBody List<InvestigationValidationRequest> requests) {
        log.info("POST /validation/validate called, requestCount={}", requests != null ? requests.size() : 0);
        ApiResponse<String> stringApiResponse = validationService.validateInvestigations(requests);
        log.info("POST /validation/validate completed");
        return   ResponseEntity.ok(stringApiResponse);
    }

    @GetMapping("/resultStatus")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getValidated() {
        log.info("GET /validation/resultStatus API called");
        ApiResponse<List<ResultResponse>> responseList = validationService.getValidatedResultEntries();
        log.info("GET /validation/resultStatus API completed");
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/saveResultEntry")
    public ResponseEntity<ApiResponse<String>> saveOrUpdate(@RequestBody ResultEntryMainRequest request) {
        log.info("Received saveOrUpdateResultEntry request for sampleCollectionHeaderId={}, subChargeCodeId={}",
                request.getSampleCollectionHeaderId(), request.getSubChargeCodeId());
        ApiResponse<String> response = resultService.saveOrUpdateResultEntry(request);
        log.info("saveOrUpdateResultEntry response status={}", response.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unvalidated")
    public ApiResponse<List<DgResultEntryValidationResponse>> getAllUnvalidatedResults() {
        log.info("Received request to fetch all unvalidated results");
        return  resultService.getUnvalidatedResults();
    }

    @PutMapping("/validate")
    public ApiResponse<String> updateResultValidation(@RequestBody ResultValidationUpdateRequest request) {
        log.info("Received request to validate result. HeaderId={}", request.getResultEntryHeaderId());
        return resultService.updateResultValidation(request);
    }

    @GetMapping("/getUpdate")
    public ApiResponse<List<ResultEntryUpdateResponse>> getUpdate() {
        log.info("getUpdate api called");
        return  resultService.getUpdate();
    }

    @PutMapping("/update")
    public ApiResponse<String> updateResult(@RequestBody ResultUpdateRequest request) {
        log.info("Received request to update result. HeaderId={}", request.getResultEntryHeaderId());
        return resultService.updateResult(request);

    }
    @PostMapping("/registration/billing")
    public ApiResponse<AppsetupResponse> labRegistrationForExistingOrder(
            @RequestBody LabBillingOnlyRequest labReq) {
        log.info("Received lab registration billing request for existing order. OrderHdId={}",
                labReq.getOrderhdid());
        return labRegistrationServices.labRegForExistingOrder(labReq);
    }

    @GetMapping("/billingStatus")
    public ApiResponse<List<BillingHeaderResponse>> getBillingStatus() {
        log.info("billingStatus api called");
        return  billingService.getBillingStatus();
    }

    @PostMapping("/track-order-status/create")
    public ResponseEntity<?> create(@Valid @RequestBody LabOrderTrackingStatusRequest request) {
        log.info("track-order-status/create api called");
        return ResponseEntity.status(HttpStatus.CREATED).body(labOrderTrackingStatusService.create(request));
    }


}
