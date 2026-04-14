package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.PackageRateConfig;
import com.hims.entity.User;
import com.hims.entity.repository.*;
import com.hims.projection.PackageRateConfigProjection;
import com.hims.request.PackageRateConfigRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PackageRateConfigResponse;
import com.hims.service.PackageRateConfigService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PackageRateConfigServiceImpl implements PackageRateConfigService {
    @Autowired
    private PackageRateConfigRepository repository;
    @Autowired
    private MasIpdPackageRepository packageRepository;
    @Autowired
    private MasIpdBillingTypeRepository billingTypeRepository;
    @Autowired
    private MasInsuranceRepository insuranceRepository;
    @Autowired
    private MasTpaRepository tpaRepository;
    @Autowired
    private MasCorporateRepository corporateRepository;
    @Autowired
    private MasRoomCategoryRepo roomCategoryRepository;
    @Autowired
    private AuthUtil authUtil;
    @Override
    @Transactional
    public ApiResponse<PackageRateConfigResponse> savePackageRateConfig(PackageRateConfigRequest request) {

        User user = authUtil.getCurrentUser();
        try {
            PackageRateConfig entity = new PackageRateConfig();

            mapRequestToEntity(entity, request);

            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdatedDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<PackageRateConfigResponse> updatePackageRateConfig(Long id, PackageRateConfigRequest request) {

        try {
            User user = authUtil.getCurrentUser();
            PackageRateConfig entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Record not found", HttpStatus.NOT_FOUND.value());
            }
            mapRequestToEntity(entity, request);

            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdatedDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error updating PackageRateConfig id: {}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<PackageRateConfigResponse> changeStatus(Long id, String status) {

        try {
            PackageRateConfig entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Record not found", HttpStatus.NOT_FOUND.value());
            }

            User user = authUtil.getCurrentUser();
            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdatedDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error changing status id: {}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PackageRateConfigResponse> getByIdPackageRateConfig(Long id) {
        try {

            PackageRateConfigProjection p = repository.getByIdPackageRateConfig(id);

            if (p == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Data not found",
                        HttpStatus.NOT_FOUND.value()
                );
            }

            PackageRateConfigResponse response = convertToResponse(p);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<PackageRateConfigResponse>> getByAllPackageRateConfigId(
            Long billingTypeId, Long corporateId, Long insuranceId,
            String search, int page, int size) {

        try {
            String packageName = (search == null) ? null : "%" + search.toLowerCase() + "%";

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastUpdatedDate"));
            Page<PackageRateConfigProjection> result = repository.getByAllPackageRateConfigId(
                            billingTypeId,
                            corporateId,
                            insuranceId,
                            packageName,
                            pageable
                    );

            Page<PackageRateConfigResponse> response = result.map(this::convertToResponse);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    private void mapRequestToEntity(PackageRateConfig entity, PackageRateConfigRequest request) {

        entity.setIpdPackage(packageRepository.findById(request.getPackageId()).orElse(null));
        entity.setBillingType(billingTypeRepository.findById(request.getBillingTypeId()).orElse(null));
        entity.setInsuranceId(insuranceRepository.findById(request.getInsuranceId()).orElse(null));
        entity.setTpa(tpaRepository.findById(request.getTpaId()).orElse(null));
        entity.setCorporate(corporateRepository.findById(request.getCorporateId()).orElse(null));
        entity.setMasRoomCategory(roomCategoryRepository.findById(request.getRoomCategoryId()).orElse(null));
        entity.setAmount(request.getAmount());
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setPreauthRequired(request.getPreAuthRequired());
        entity.setCopayPercent(request.getCopayPercent());
        entity.setMaxClaimAmount(request.getMaxClaimAmount());
    }
    private PackageRateConfigResponse mapToResponse(PackageRateConfig entity) {

        PackageRateConfigResponse res = new PackageRateConfigResponse();
        res.setConfigId(entity.getConfigId());
        res.setPackageId(entity.getIpdPackage().getPackageId());
        res.setPackageName(entity.getIpdPackage().getPackageName());
        res.setBillingTypeId(entity.getBillingType().getBillingTypeId());
        res.setBillingTypeName(entity.getBillingType().getBillingTypeName());
        res.setInsuranceId(entity.getInsuranceId().getInsuranceId());
        res.setInsuranceName(entity.getInsuranceId().getInsuranceName());
        res.setTpaId(entity.getTpa().getTpaId());
        res.setTpaName(entity.getTpa().getTpaName());
        res.setCorporateId(entity.getCorporate().getCorporateId());
        res.setCorporateName(entity.getCorporate().getCorporateName());
        res.setRoomCategoryId(entity.getMasRoomCategory().getRoomCategoryId());
        res.setRoomCategoryName(entity.getMasRoomCategory().getRoomCategoryName());
        res.setAmount(entity.getAmount());
        res.setEffectiveFrom(entity.getEffectiveFrom());
        res.setEffectiveTo(entity.getEffectiveTo());
        res.setPreAuthRequired(entity.getPreauthRequired());
        res.setCopayPercent(entity.getCopayPercent());
        res.setMaxClaimAmount(entity.getMaxClaimAmount());
        res.setStatus(entity.getStatus());
        return res;
    }
    private PackageRateConfigResponse convertToResponse(PackageRateConfigProjection p) {
        PackageRateConfigResponse r = new PackageRateConfigResponse();
        r.setConfigId(p.getConfigId());
        r.setPackageId(p.getPackageId());
        r.setPackageName(p.getPackageName());

        r.setBillingTypeId(p.getBillingTypeId());
        r.setBillingTypeName(p.getBillingTypeName());

        r.setInsuranceId(p.getInsuranceId());
        r.setInsuranceName(p.getInsuranceName());

        r.setTpaId(p.getTpaId());
        r.setTpaName(p.getTpaName());

        r.setCorporateId(p.getCorporateId());
        r.setCorporateName(p.getCorporateName());

        r.setRoomCategoryId(p.getRoomCategoryId());
        r.setRoomCategoryName(p.getRoomCategoryName());

        r.setAmount(p.getAmount());
        r.setEffectiveFrom(p.getEffectiveFrom());
        r.setEffectiveTo(p.getEffectiveTo());

        r.setPreAuthRequired(p.getPreAuthRequired());

        r.setCopayPercent(p.getCopayPercent());
        r.setMaxClaimAmount(p.getMaxClaimAmount());

        r.setStatus(p.getStatus());

        return r;
    }
}
