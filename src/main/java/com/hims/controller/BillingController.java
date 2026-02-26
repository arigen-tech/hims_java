package com.hims.controller;

import com.hims.request.PaymentUpdateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PaymentResponse;
import com.hims.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {
    @Autowired
    private final BillingService billingService;


    //consultation Services
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(@RequestBody PaymentUpdateRequest request) {
        return new ResponseEntity<>(billingService.updatePayment(request), HttpStatus.OK);
    }

    //Lab or Radiology Services
    @PostMapping("/updatepaymentstatus")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentStatusResponse(@RequestBody PaymentUpdateRequest request) {
        log.info("Update Payment Status API called");
        if(request.getBillingType()!=null)
            if(request.getBillingType().equalsIgnoreCase("Radiology Services"))
                return new ResponseEntity<>(billingService.paymentStatusReq(request), HttpStatus.OK);
        return new ResponseEntity<>(billingService.paymentStatusReqLab(request), HttpStatus.OK);
    }
}
