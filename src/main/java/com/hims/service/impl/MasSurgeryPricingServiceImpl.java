package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasSurgeryPricing;
import com.hims.entity.User;
import com.hims.entity.repository.MasIpdBillingTypeRepository;
import com.hims.entity.repository.MasSurgeryPricingRepository;
import com.hims.entity.repository.MasSurgeryRepository;
import com.hims.projection.MasSurgeryPricingProjection;
import com.hims.request.MasSurgeryPricingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSurgeryPricingResponse;
import com.hims.service.MasSurgeryPricingService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MasSurgeryPricingServiceImpl implements MasSurgeryPricingService {

    @Autowired
    private MasSurgeryPricingRepository repository;

    @Autowired
    private MasSurgeryRepository surgeryRepository;

    @Autowired
    private MasIpdBillingTypeRepository billingTypeRepository;

    @Autowired
    private AuthUtil authUtil;


    @Override
    public ApiResponse<String> addMasSurgeryPricing(MasSurgeryPricingRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasSurgeryPricing entity = new MasSurgeryPricing();
            entity.setSurgery(surgeryRepository.findById(request.getSurgeryId()).orElseThrow());
            entity.setBillingType(billingTypeRepository.findById(request.getBillingTypeId()).orElseThrow());
            entity.setAmount(request.getAmount());
            entity.setDiscountAllowed(request.getDiscountAllowed());
            entity.setEffectiveFrom(request.getEffectiveFrom());
            entity.setEffectiveTo(request.getEffectiveTo());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse("Surgery pricing added successfully", new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<String> updateMasSurgeryPricing(Long id, MasSurgeryPricingRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasSurgeryPricing entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Surgery pricing not found", HttpStatus.NOT_FOUND.value());
            }

            entity.setSurgery(surgeryRepository.findById(request.getSurgeryId()).orElseThrow());
            entity.setBillingType(billingTypeRepository.findById(request.getBillingTypeId()).orElseThrow());
            entity.setAmount(request.getAmount());
            entity.setDiscountAllowed(request.getDiscountAllowed());
            entity.setEffectiveFrom(request.getEffectiveFrom());
            entity.setEffectiveTo(request.getEffectiveTo());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse("Updated successfully", new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<String> changeStatusMasSurgeryPricing(Long id, String status) {
        try {
            User user = authUtil.getCurrentUser();

            MasSurgeryPricing entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invalid Id"));

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse("Status updated", new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<MasSurgeryPricingResponse> getByIdMasSurgeryPricing(Long id) {
        try {
            MasSurgeryPricing entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Not found"));

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<Page<MasSurgeryPricingResponse>> getAllMasSurgeryPricing(Long billingTypeId, String surgeryName, int page, int size) {

        try {
            String name = (surgeryName == null) ? null : "%" + surgeryName.toLowerCase() + "%";

            Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdateDate").descending());

            Page<MasSurgeryPricingProjection> result = repository.getAllMasSurgeryPricing(billingTypeId, name, pageable);

            Page<MasSurgeryPricingResponse> response = result.map(this::toResponse);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    private MasSurgeryPricingResponse mapToResponse(MasSurgeryPricing entity) {
        MasSurgeryPricingResponse res = new MasSurgeryPricingResponse();

        res.setSurgeryPricingId(entity.getSurgeryPricingId());
        res.setSurgeryId(entity.getSurgery().getSurgeryId());
        res.setSurgeryName(entity.getSurgery().getSurgeryName());
        res.setAmount(entity.getAmount());
        res.setDiscountAllowed(entity.getDiscountAllowed());
        res.setEffectiveFrom(entity.getEffectiveFrom());
        res.setEffectiveTo(entity.getEffectiveTo());
        res.setStatus(entity.getStatus());
        res.setBillingTypeId(entity.getBillingType().getBillingTypeId());
        res.setBillingTypeName(entity.getBillingType().getBillingTypeName());

        return res;
    }
    private MasSurgeryPricingResponse toResponse(MasSurgeryPricingProjection p) {

        MasSurgeryPricingResponse res = new MasSurgeryPricingResponse();

        res.setSurgeryPricingId(p.getSurgeryPricingId());
        res.setSurgeryId(p.getSurgeryId());
        res.setSurgeryName(p.getSurgeryName());
        res.setAmount(p.getAmount());
        res.setDiscountAllowed(p.getDiscountAllowed());
        res.setEffectiveFrom(p.getEffectiveFrom());
        res.setEffectiveTo(p.getEffectiveTo());
        res.setStatus(p.getStatus());
        res.setBillingTypeId(p.getBillingTypeId());
        res.setBillingTypeName(p.getBillingTypeName());

        return res;
    }
}