package com.hims.service;

import com.hims.entity.*;
import com.hims.response.ApiResponse;
import com.hims.response.OpdBillingPaymentResponse;
import com.hims.response.PendingBillingResponse;
import com.hims.response.PendingBillingSearchResponse;

import java.time.LocalDate;
import java.util.List;

public interface BillingService {
    ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount);

    ApiResponse<List<PendingBillingResponse>> getPendingBilling();

    public List<PendingBillingSearchResponse> searchPendingBilling(String patientName, String uhidNo);
}
