package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasPatientAcuity;
import com.hims.entity.User;
import com.hims.entity.repository.MasPatientAcuityRepository;
import com.hims.request.MasPatientAcuityRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasPatientAcuityResponse;
import com.hims.response.UserContext;
import com.hims.service.MasPatientAcuityService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasPatientAcuityServiceImpl implements MasPatientAcuityService {
    @Autowired
    private MasPatientAcuityRepository repository;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<MasPatientAcuityResponse>> getAll(int flag) {
        try {
            List<MasPatientAcuity> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByAcuityNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            List<MasPatientAcuityResponse> response = list.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Something went wrong: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasPatientAcuityResponse> getById(Long id) {
        try {
            MasPatientAcuity acuity = repository.findById(id).orElse(null);
            if (acuity == null)
                return ResponseUtils.createNotFoundResponse("Acuity ID not found!", 404);

            return ResponseUtils.createSuccessResponse(toResponse(acuity), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasPatientAcuityResponse> create(MasPatientAcuityRequest request) {
        try {
            UserContext userContext = userContextService.getCurrentUserContext();

            MasPatientAcuity acuity = MasPatientAcuity.builder()
                    .acuityCode(request.getAcuityCode())
                    .acuityName(request.getAcuityName())
                    .description(request.getDescription())
                    .status(AppConstants.STATUS_Y.toLowerCase())
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            MasPatientAcuity saved = repository.save(acuity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Creation failed: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasPatientAcuityResponse> update(Long id, MasPatientAcuityRequest request) {
        try {
            MasPatientAcuity acuity = repository.findById(id).orElse(null);
            if (acuity == null)
                return ResponseUtils.createNotFoundResponse("Acuity ID not found!", 404);

            UserContext userContext = userContextService.getCurrentUserContext();

            acuity.setAcuityCode(request.getAcuityCode());
            acuity.setAcuityName(request.getAcuityName());
            acuity.setDescription(request.getDescription());
            acuity.setLastUpdatedBy(userContext.getUserFullName());
            acuity.setLastUpdateDate(LocalDateTime.now());

            repository.save(acuity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(acuity), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Update failed: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasPatientAcuityResponse> changeStatus(Long id, String status) {
        try {
            MasPatientAcuity acuity = repository.findById(id).orElse(null);
            if (acuity == null)
                return ResponseUtils.createNotFoundResponse("Acuity ID not found!", 404);

            if (!status.equals("y") && !status.equals("n"))
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {}, "Invalid status!", 400);

            UserContext userContext = userContextService.getCurrentUserContext();
            acuity.setStatus(status);
            acuity.setLastUpdatedBy(userContext.getUserFullName());
            acuity.setLastUpdateDate(LocalDateTime.now());

            repository.save(acuity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(acuity), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Status update failed: " + e.getMessage(), 500);
        }
    }

    private MasPatientAcuityResponse toResponse(MasPatientAcuity m) {
        return new MasPatientAcuityResponse(
                m.getAcuityCode(),
                m.getAcuityName(),
                m.getDescription(),
                m.getStatus(),
                m.getLastUpdateDate(),
                m.getCreatedBy(),
                m.getLastUpdatedBy()
        );
    }


}
