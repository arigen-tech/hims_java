package com.hims.controller;

import com.hims.projection.BillingHeaderResponseProjection;
import com.hims.request.PaidCancelledAppointmentResponse;
import com.hims.request.PaymentUpdateRequest;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.service.MasHospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    @Value("${serviceCategoryRad}")
    private String radioServiceCategoryCode;

    private final BillingService billingService;

    private final MasHospitalService hospitalService;




    /**
     * Process OPD Consultation payment
     */
    @PostMapping("/processOpdPayment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processOpdPayment(
            @RequestBody PaymentUpdateRequest request) {
        return new ResponseEntity<>(billingService.processOpdPayment(request), HttpStatus.OK);
    }

    /**
     * Process Lab payment
     */
    @PostMapping("/processLabPayment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processLabPayment(
            @RequestBody PaymentUpdateRequest request) {
        log.info("Process Lab Payment API called");
        return new ResponseEntity<>(billingService.processLabPayment(request), HttpStatus.OK);
    }

    /**
     * Process Radiology payment
     */
    @PostMapping("/processRadiologyPayment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processRadiologyPayment(
            @RequestBody PaymentUpdateRequest request) {
        log.info("Process Radiology Payment API called");
        return new ResponseEntity<>(billingService.processRadiologyPayment(request), HttpStatus.OK);
    }

    /**
     * Get pending billing patients filtered by service category
     */
    @GetMapping("/pendingBillingsByCategory/{categoryCode}")
    public ApiResponse<?> getPendingBillingsByCategory(
            @PathVariable String categoryCode,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return billingService.getPendingBillingsByCategory(
                categoryCode, patientName, mobileNo, registrationNo, page, size);
    }

    /**
     * Get OPD billing details for a specific patient
     */
    @GetMapping("/OPDPatientBillDetails/{patientId}")
    public ApiResponse<PatientAppointmentResponse> getOPDPatientBillDetails(
            @PathVariable Long patientId) {
        log.info("Get OPD Patient Bill Details API called for patientId={}", patientId);
        return billingService.getOPDPatientBillDetails(patientId);
    }

    /**
     * Get Lab/Radiology billing details by billing header ID
     */
    @GetMapping("/getLabRadiologyBillingDetails/{billingHdId}")
    public ApiResponse<List<PendingBillingResponse>> getLabRadiologyBillingDetails(
            @PathVariable Long billingHdId,
            @RequestParam String serviceCategoryCode) {
        log.info("Get Lab/Radiology Billing Details API called for billingHdId={}", billingHdId);
        return billingService.getLabRadiologyBillingDetails(billingHdId, serviceCategoryCode);
    }

    /**
     * Search invoice details by patient name, phone or registration number
     */
    @GetMapping("/searchInvoiceDetails")
    public ApiResponse<Page<BillingHeaderResponseProjection>> searchInvoiceDetails(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phoneNo,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(required = false) Long serviceCategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        log.info("Search Invoice Details API called - patientName={}, phoneNo={}, registrationNo={},serviceCategoryId = {}",
                patientName, phoneNo, registrationNo,serviceCategoryId);
        return billingService.searchInvoiceDetails(patientName, phoneNo, registrationNo,serviceCategoryId, page,size);
    }

    @GetMapping("/billingRefundPatientList")
    public ResponseEntity<ApiResponse<Page<PaidCancelledAppointmentResponse>>> getBillingRefundPatientList(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String patientName,
        @RequestParam(required = false) String mobileNo,
        @RequestParam(required = false) String billingServiceType,
        @RequestParam(required = false) String refundStatus,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Billing refund patient list request received");
        ApiResponse<Page<PaidCancelledAppointmentResponse>> response =
                billingService.getBillingRefundPatientList(page, size, patientName, mobileNo, billingServiceType, refundStatus, fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/patientBillingRefundDetails/{billingId}"})
    public ResponseEntity<ApiResponse<List<PatientBillingRefundDetailsResponse>>>
    getPatientBillingRefundDetails(@PathVariable Long billingId) {
        log.info("Fetching refund details for billingId={}", billingId);
        return ResponseEntity.ok(billingService.getPatientBillingRefundDetails(billingId));
    }

    @GetMapping("/billingConfig/{hospitalId}")
    public  ResponseEntity<?> getBillingConfigForHospital(@PathVariable Long hospitalId){
        return  ResponseEntity.ok(hospitalService.getBillingConfig(hospitalId));
    }
}
