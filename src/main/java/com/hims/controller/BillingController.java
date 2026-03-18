package com.hims.controller;

import com.hims.request.PaymentUpdateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PatientAppointmentResponse;
import com.hims.response.PaymentResponse;
import com.hims.response.PendingBillingResponse;
import com.hims.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/updatePaymentStatus")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentStatusResponse(@RequestBody PaymentUpdateRequest request) {
        log.info("Update Payment Status API called");
        if(request.getBillingType()!=null)
            if(request.getBillingType().equalsIgnoreCase("Radiology Services"))
                return new ResponseEntity<>(billingService.paymentStatusReq(request), HttpStatus.OK);
        return new ResponseEntity<>(billingService.paymentStatusReqLab(request), HttpStatus.OK);
    }

    @GetMapping("/pendingBillingPatients")
    public ApiResponse<List<PendingBillingResponse>> getPendingBilling() {
        log.info("Get Pending Billing API called");
        return billingService.getPendingBilling();
    }

    @GetMapping("/CatagoryWiseBilling/{serviceCategoryCode}")
    public ApiResponse<?> getBillingPatientsByCatagory(
            @PathVariable String serviceCategoryCode,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return billingService.getBillingPatientsByCatagory(
                serviceCategoryCode, patientName, mobileNo, registrationNo, page, size);
    }

    @GetMapping("/patientBillingDetails/{patientId}")
    public ApiResponse<PatientAppointmentResponse> getBillingDetails(@PathVariable Long patientId) {
        log.info("Get Pending Billing API called");
        return billingService.getBillingDetails(patientId);
    }

    @GetMapping("/pendingBillingLabRadioDetails/{billingHdId}")
    public ApiResponse<List<PendingBillingResponse>> getPendingBillingLabRadio(@PathVariable Long billingHdId,@RequestParam String serviceCategoryCode){
        log.info("Get Pending Billing API called for Lab Radio");
        return billingService.getPendingBillingLabRadio(billingHdId, serviceCategoryCode);
    }


}
