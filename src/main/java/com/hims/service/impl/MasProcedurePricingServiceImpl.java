package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasIpdBillingType;
import com.hims.entity.MasProcedure;
import com.hims.entity.MasProcedurePricing;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasIpdBillingTypeRepository;
import com.hims.entity.repository.MasProcedurePricingRepository;
import com.hims.entity.repository.MasProcedureRepository;
import com.hims.projection.MasProcedurePricingProjection;
import com.hims.request.MasProcedurePricingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedurePricingResponse;
import com.hims.service.MasProcedurePricingService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class MasProcedurePricingServiceImpl implements MasProcedurePricingService {
    @Autowired
    private MasIpdBillingTypeRepository masIpdBillingTypeRepository;
    @Autowired
    private MasProcedureRepository masProcedureRepository;
    @Autowired
    private MasDepartmentRepository masDepartmentRepository;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private MasProcedurePricingRepository masProcedurePricingRepository;

    @Override
    public ApiResponse<String> addMasProcedurePricing(MasProcedurePricingRequest request) {

        try {
            User user = authUtil.getCurrentUser();
            MasProcedurePricing entity = new MasProcedurePricing();
            entity.setProcedure( masProcedureRepository.findById(request.getProcedureId()).orElseThrow());
            entity.setBasePrice(request.getBasePrice());
            entity.setDiscountAllowed(request.getDiscountAllowed());
            entity.setEffectiveFrom(request.getEffectiveFrom());
            entity.setEffectiveTo(request.getEffectiveTo());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            entity.setDiscount(request.getDiscount());
            entity.setBillingTypeId(masIpdBillingTypeRepository.findById(request.getBillingTypeId()).orElseThrow());
            masProcedurePricingRepository.save(entity);
            return ResponseUtils.createSuccessResponse("MasProcedurePricing add successfully", new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Mas Procedure Pricing error",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> updateMasProcedurePricing(Long id, MasProcedurePricingRequest request) {

        try {
            User user = authUtil.getCurrentUser();
            MasProcedurePricing entity=masProcedurePricingRepository.findById(id).orElse(null);
            if(entity==null){
                return ResponseUtils.createNotFoundResponse("Procedure pricing id not found", HttpStatus.NOT_FOUND.value());
            }
            entity.setProcedure( masProcedureRepository.findById(request.getProcedureId()).orElseThrow());
            entity.setBasePrice(request.getBasePrice());
            entity.setDiscountAllowed(request.getDiscountAllowed());
            entity.setEffectiveFrom(request.getEffectiveFrom());
            entity.setEffectiveTo(request.getEffectiveTo());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            entity.setDiscount(request.getDiscount());
            entity.setBillingTypeId(masIpdBillingTypeRepository.findById(request.getBillingTypeId()).orElseThrow());
            masProcedurePricingRepository.save(entity);
            return ResponseUtils.createSuccessResponse("MasProcedurePricing update successfully", new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Mas Procedure Pricing error",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<String> changeStatusMasProcedurePricing(Long id, String status) {

        try {
            User user = authUtil.getCurrentUser();
            MasProcedurePricing entity = masProcedurePricingRepository.findById(id).orElseThrow(() -> new RuntimeException("Invalid Id"));
            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            masProcedurePricingRepository.save(entity);
            return ResponseUtils.createSuccessResponse("status change successfully", new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Mas Procedure Pricing error",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasProcedurePricingResponse> getByIdMasProcedurePricing(Long id) {
        try {
            MasProcedurePricing entity = masProcedurePricingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Procedure Pricing not found with id: " + id));

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Mas Procedure Pricing error",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<MasProcedurePricingResponse>> getAllMasProcedurePricing(Long billingTypeId, String procedureName, int page, int size) {
        try {
            String procedure= (procedureName == null) ? null : "%" + procedureName.toLowerCase() + "%";
            Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdateDate").descending());
            Page<MasProcedurePricingProjection> result = masProcedurePricingRepository.getAllMasProcedurePricing(billingTypeId, procedure, pageable);
            Page<MasProcedurePricingResponse> response = result.map(this::toResponse);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Mas Procedure Pricing error",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasProcedurePricingResponse mapToResponse(MasProcedurePricing entity) {

        MasProcedurePricingResponse res = new MasProcedurePricingResponse();
        res.setProcedurePricingId(entity.getProcedurePricingId());
        res.setProcedureId(entity.getProcedure()!=null?entity.getProcedurePricingId():null);
        res.setProcedureName(entity.getProcedure()!=null?entity.getProcedure().getProcedureName():null);
        res.setBasePrice(entity.getBasePrice());
        res.setDiscountAllowed(entity.getDiscountAllowed());
        res.setEffectiveFrom(entity.getEffectiveFrom());
        res.setEffectiveTo(entity.getEffectiveTo());
        res.setDiscount(entity.getDiscount());
        res.setStatus(entity.getStatus());
        res.setBillingTypeId(entity.getBillingTypeId()!=null?entity.getBillingTypeId().getBillingTypeId():null);
        res.setBillingTypeName(entity.getBillingTypeId()!=null?entity.getBillingTypeId().getBillingTypeName():null);
        return res;
    }
    private MasProcedurePricingResponse toResponse(MasProcedurePricingProjection p) {
        MasProcedurePricingResponse res = new MasProcedurePricingResponse();
        res.setProcedurePricingId(p.getProcedurePricingId());
        res.setProcedureId(p.getProcedureId());
        res.setProcedureName(p.getProcedureName());
        res.setBasePrice(p.getBasePrice());
        res.setDiscountAllowed(p.getDiscountAllowed());
        res.setEffectiveFrom(p.getEffectiveFrom());
        res.setEffectiveTo(p.getEffectiveTo());
        res.setDiscount(p.getDiscount());
        res.setStatus(p.getStatus());
        res.setBillingTypeId(p.getBillingTypeId());
        res.setBillingTypeName(p.getBillingTypeName());
        return res;
    }
}
