package com.hims.service;

import com.beust.ah.A;
import com.hims.entity.*;
import com.hims.projection.BillingHeaderResponseProjection;
import com.hims.request.PaymentUpdateRequest;
import com.hims.response.*;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

public interface BillingService {
    ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount);

    ApiResponse<List<PendingBillingResponse>> getPendingBilling();

    

    ApiResponse<PatientAppointmentResponse> getBillingDetails(Long patientId);

    ApiResponse<List<BillingHeaderResponseProjection>> getBillingStatus(String patientName, String phoneNo, String registrationNo);

    //Update the Consultation services payment status
    ApiResponse<PaymentResponse> updatePayment(PaymentUpdateRequest opdreq);

    //Radiology Services
    @Transactional
    ApiResponse paymentStatusReq(PaymentUpdateRequest request);

    //Laboratory
    ApiResponse<PaymentResponse> paymentStatusReqLab(PaymentUpdateRequest labreq);

    ApiResponse<?> getBillingPatientsByCatagory(String serviceCategoryCode, int page, int size);
}
