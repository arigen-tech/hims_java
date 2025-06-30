package com.hims.service;

import com.hims.entity.*;
import com.hims.response.ApiResponse;
import com.hims.response.OpdBillingPaymentResponse;

public interface BillingService {
    ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount);
}
