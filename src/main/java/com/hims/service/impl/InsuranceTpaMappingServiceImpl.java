package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.InsuranceTpaMapping;
import com.hims.entity.MasInsurance;
import com.hims.entity.MasTpa;
import com.hims.entity.User;
import com.hims.entity.repository.InsuranceTpaMappingRepository;
import com.hims.entity.repository.MasInsuranceRepository;
import com.hims.entity.repository.MasTpaRepository;
import com.hims.request.InsuranceTpaMappingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.InsuranceTpaMappingResponse;
import com.hims.service.InsuranceTpaMappingService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InsuranceTpaMappingServiceImpl implements InsuranceTpaMappingService {
    @Autowired
    private InsuranceTpaMappingRepository repository;
    @Autowired
    private MasInsuranceRepository masInsuranceRepository;
    @Autowired
    private MasTpaRepository masTpaRepository;

    @Autowired
    private AuthUtil authUtil;


    @Override
    public ApiResponse<List<InsuranceTpaMappingResponse>> getAllInsuranceTpaMapping(int flag) {

        log.info("Fetching Insurance TPA Mapping list");

        try {

            List<InsuranceTpaMapping> list = (flag == 1) ? repository.findByStatusIgnoreCaseOrderByCreatedAtDesc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescUpdatedAtDesc();

            return ResponseUtils.createSuccessResponse(list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error fetching Insurance TPA Mapping list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<InsuranceTpaMappingResponse> getByIdInsuranceTpaMapping(Long id) {

        log.info("Fetching Insurance TPA Mapping by id={}", id);

        try {

            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            mapToResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Insurance TPA Mapping not found",
                            404
                    ));

        } catch (Exception e) {
            log.error("Error fetching Insurance TPA Mapping", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<InsuranceTpaMappingResponse> createInsuranceTpaMapping(InsuranceTpaMappingRequest request) {

        log.info("Creating Insurance TPA Mapping");

        try {

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found", 404
                );
            }
            InsuranceTpaMapping entity = new InsuranceTpaMapping();
            Optional<MasInsurance> masInsurance=masInsuranceRepository.findById(request.getInsuranceId());
            if(masInsurance.isEmpty()){

                return ResponseUtils.createNotFoundResponse("Insurance   not found", 404
                );
            }
            Optional<MasTpa> masTpa=masTpaRepository.findById(request.getTpaId());
            if(masTpa.isEmpty()){
                return ResponseUtils.createNotFoundResponse("TPA  not found", 404
                );
            }
            entity.setInsurance(masInsurance.get());
            entity.setTpa(masTpa.get());
            entity.setEffectiveFrom(request.getEffectiveFrom());
            entity.setEffectiveTo(request.getEffectiveTo());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedBy(user.getFullName());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setUpdatedBy(user.getFullName());
            entity.setMode(request.getMode());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error creating Insurance TPA Mapping", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<InsuranceTpaMappingResponse> updateInsuranceTpaMapping(Long id, InsuranceTpaMappingRequest request) {

        log.info("Updating Insurance TPA Mapping id={}", id);

        try {
            InsuranceTpaMapping entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Insurance TPA Mapping not found", 404
                );
            }

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found",
                        404
                );
            }
            Optional<MasInsurance> masInsurance=masInsuranceRepository.findById(request.getInsuranceId());
            if(masInsurance.isEmpty()){

                return ResponseUtils.createNotFoundResponse("Insurance   not found", 404
                );
            }
            Optional<MasTpa> masTpa=masTpaRepository.findById(request.getTpaId());
            if(masTpa.isEmpty()){
                return ResponseUtils.createNotFoundResponse("TPA  not found", 404
                );
            }
            entity.setInsurance(masInsurance.get());
            entity.setTpa(masTpa.get());
            entity.setEffectiveFrom(request.getEffectiveFrom());
            entity.setEffectiveTo(request.getEffectiveTo());
            entity.setUpdatedBy(user.getFullName());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setMode(request.getMode());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error updating Insurance TPA Mapping", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<InsuranceTpaMappingResponse> changeStatusInsuranceTpaMapping(Long id, String status) {
        log.info("Changing status of Insurance TPA Mapping");

        try {
            InsuranceTpaMapping entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Insurance TPA Mapping not found", 404);
            }
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found", 404);
            }

            entity.setStatus(status.toLowerCase());
            entity.setUpdatedBy(user.getFullName());
            entity.setUpdatedAt(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error changing Insurance TPA Mapping status", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }
    }

    private InsuranceTpaMappingResponse mapToResponse(InsuranceTpaMapping entity) {
        InsuranceTpaMappingResponse res = new InsuranceTpaMappingResponse();
        res.setMappingId(entity.getMappingId());
        res.setInsuranceId(entity.getInsurance().getInsuranceId());
        res.setInsuranceName(entity.getInsurance().getInsuranceName());
        res.setTpaId(entity.getTpa().getTpaId());
        res.setTpaName(entity.getTpa().getTpaName());
        res.setEffectiveFrom(entity.getEffectiveFrom());
        res.setEffectiveTo(entity.getEffectiveTo());
        res.setStatus(entity.getStatus());
        res.setMode(entity.getMode());
        return res;
    }
}
