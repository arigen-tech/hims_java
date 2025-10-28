package com.hims.service;

import com.hims.entity.*;
import com.hims.response.ApiResponse;
import com.hims.response.OpdBillingPaymentResponse;
import com.hims.response.PendingBillingResponse;


import java.util.List;

public interface BillingService {
    ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount);

    ApiResponse<List<PendingBillingResponse>> getPendingBilling();

}
