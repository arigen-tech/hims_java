package com.hims.service;

import com.hims.request.BillingPolicyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.BillingPolicyResponse;

import java.util.List;

public interface BillingPolicyService {
    ApiResponse<List<BillingPolicyResponse>> getAll();

    ApiResponse<BillingPolicyResponse> getById(Long id);

    ApiResponse<BillingPolicyResponse> create(BillingPolicyRequest request);

    ApiResponse<BillingPolicyResponse> update(Long id, BillingPolicyRequest request);

   // ApiResponse<BillingPolicyResponse> changeStatus(Long id, String status);
}
