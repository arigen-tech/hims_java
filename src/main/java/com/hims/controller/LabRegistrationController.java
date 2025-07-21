package com.hims.controller;

import com.hims.request.LabRegRequest;
import com.hims.request.PaymentUpdateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.PaymentResponse;
import com.hims.response.PendingBillingResponse;
import com.hims.service.BillingService;
import com.hims.service.LabRegistrationServices;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
