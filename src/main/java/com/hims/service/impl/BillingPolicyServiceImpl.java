package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.BillingPolicyMaster;
import com.hims.entity.User;
import com.hims.entity.repository.BillingPolicyRepository;
import com.hims.request.BillingPolicyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.BillingPolicyResponse;
import com.hims.service.BillingPolicyService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillingPolicyServiceImpl implements BillingPolicyService {

    @Autowired
    private BillingPolicyRepository repo;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<BillingPolicyResponse>> getAll() {
        try {
            List<BillingPolicyMaster> list =repo.findAllByOrderByLastUpdateDateDesc();


            List<BillingPolicyResponse> response =
                    list.stream().map(this::toResponse).toList();

            return ResponseUtils.createSuccessResponse(
                    response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<BillingPolicyResponse> getById(Long id) {
        BillingPolicyMaster policy = repo.findById(id).orElse(null);

        if (policy == null)
            return ResponseUtils.createNotFoundResponse(
                    "Billing Policy not found", 404);

        return ResponseUtils.createSuccessResponse(
                toResponse(policy), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<BillingPolicyResponse> create(BillingPolicyRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            BillingPolicyMaster policy = BillingPolicyMaster.builder()
                    .policyCode(request.getPolicyCode())
                    .description(request.getDescription())
                    .applicableBillingType(request.getApplicableBillingType())
                    .followupDaysAllowed(request.getFollowupDaysAllowed())
                    .discountPercentage(request.getDiscountPercentage())
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repo.save(policy);

            return ResponseUtils.createSuccessResponse(
                    toResponse(policy), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<BillingPolicyResponse> update(
            Long id, BillingPolicyRequest request) {

        BillingPolicyMaster policy = repo.findById(id).orElse(null);

        if (policy == null)
            return ResponseUtils.createNotFoundResponse(
                    "Billing Policy not found", 404);

        User user = authUtil.getCurrentUser();

        policy.setPolicyCode(request.getPolicyCode());
        policy.setDescription(request.getDescription());
        policy.setApplicableBillingType(request.getApplicableBillingType());
        policy.setFollowupDaysAllowed(request.getFollowupDaysAllowed());
        policy.setDiscountPercentage(request.getDiscountPercentage());
        policy.setLastUpdatedBy(user.getFirstName());
        policy.setLastUpdateDate(LocalDateTime.now());

        repo.save(policy);

        return ResponseUtils.createSuccessResponse(
                toResponse(policy), new TypeReference<>() {});
    }

//    @Override
//    public ApiResponse<BillingPolicyResponse> changeStatus(
//            Long id, String status) {
//
//        BillingPolicyMaster policy = repo.findById(id).orElse(null);
//
//        if (policy == null)
//            return ResponseUtils.createNotFoundResponse(
//                    "Billing Policy not found", 404);
//
//        if (!status.equalsIgnoreCase("y")
//                && !status.equalsIgnoreCase("n"))
//            return ResponseUtils.createFailureResponse(
//                    null, new TypeReference<>() {}, "Invalid status", 400);
//
//        User user = authUtil.getCurrentUser();
//
//        policy.setStatus(status);
//        policy.setLastUpdatedBy(user.getFirstName());
//        policy.setLastUpdateDate(LocalDateTime.now());
//
//        repo.save(policy);
//
//        return ResponseUtils.createSuccessResponse(
//                toResponse(policy), new TypeReference<>() {});
//    }

    private BillingPolicyResponse toResponse(BillingPolicyMaster p) {
        return new BillingPolicyResponse(
                p.getBillingPolicyId(),
                p.getPolicyCode(),
                p.getDescription(),
                p.getApplicableBillingType(),
                p.getFollowupDaysAllowed(),
                p.getDiscountPercentage(),
                p.getCreatedBy(),
                p.getLastUpdatedBy(),
                p.getLastUpdateDate()
        );
    }
}
