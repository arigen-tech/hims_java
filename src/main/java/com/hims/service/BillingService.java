package com.hims.service;

import com.hims.entity.*;
import com.hims.projection.BillingHeaderResponseProjection;
import com.hims.request.*;
import com.hims.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BillingService {
    ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount);

    /**
     * Process OPD consultation payment
     */
    ApiResponse<PaymentResponse> processOpdPayment(PaymentUpdateRequest request);

    /**
     * Process Lab payment and update order/billing status
     */
    ApiResponse<PaymentResponse> processLabPayment(PaymentUpdateRequest request);

    /**
     * Process Radiology payment and update order/billing status
     */
    ApiResponse<PaymentResponse> processRadiologyPayment(PaymentUpdateRequest request);


    /**
     * Get pending billing patients filtered by service category (OPD / Lab / Radiology)
     */
    ApiResponse<?> getPendingBillingsByCategory(
            String categoryCode,
            String patientName,
            String mobileNo,
            String registrationNo,
            int page,
            int size);

    /**
     * Get OPD billing details for a specific patient
     */
    ApiResponse<PatientAppointmentResponse> getOPDPatientBillDetails(Long patientId);

    /**
     * Get Lab/Radiology billing details by billing header ID
     */
    ApiResponse<List<PendingBillingResponse>> getLabRadiologyBillingDetails(Long billingHdId, String serviceCategoryCode);

    /**
     * Search invoice details by patient name, phone or registration number
     */
    ApiResponse<Page<BillingHeaderResponseProjection>> searchInvoiceDetails(String patientName, String phoneNo, String registrationNo, Pageable pageable);


    BillingHeader saveBillingHeader(
            Object orderHd, Visit vId, User currentUser,
            BigDecimal sum, BigDecimal tax, BigDecimal disc,
            String serviceCategoryCode, boolean isRadiology);

    BillingDetail saveBillingDetail(
            BillingHeader bhdId,
            Object dtId,
            BigDecimal actualAmount,
            BigDecimal discountedAmount,
            String serviceCategoryCode,
            boolean isRadiology);

    BillingDetail saveBillingDetailPackage(
            BillingHeader bhdId,
            DgInvestigationPackage pack,
            LabRadioInvestigationRequest req,
            String serviceCategoryCode);


    ApiResponse<Page<PaidCancelledAppointmentResponse>>
    getBillingRefundPatientList(
            int page,
            int size,
            String patientName,
            String mobileNo,
            String billingServiceType,
            String refundStatus,
            LocalDate fromDate,
            LocalDate toDate
    );

    ApiResponse<List<PatientBillingRefundDetailsResponse>> getPatientBillingRefundDetails(Long billingHdId);


}
