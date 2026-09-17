package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.EntMasPinna;
import com.hims.entity.User;
import com.hims.entity.repository.EntMasPinnaRepository;
import com.hims.request.EntMasPinnaRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasPinnaResponse;
import com.hims.response.UserContext;
import com.hims.service.EntMasPinnaService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntMasPinnaServiceImpl implements EntMasPinnaService {

    @Autowired
    private EntMasPinnaRepository repository;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<EntMasPinnaResponse>> getAll(int flag) {
        try {
            List<EntMasPinna> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByPinnaStatusAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Pinna list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasPinnaResponse> getById(Long id) {
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Pinna not found", 404));
    }

    @Override
    public ApiResponse<EntMasPinnaResponse> create(EntMasPinnaRequest request) {

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        EntMasPinna entity = EntMasPinna.builder()
                .pinnaStatus(request.getPinnaStatus())
                .status(AppConstants.STATUS_Y.toLowerCase())
                .createdBy(userContext.getUserFullName())
                .lastUpdatedBy(userContext.getUserFullName())
                .lastUpdateDate(LocalDateTime.now())
                .build();

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<EntMasPinnaResponse> update(
            Long id, EntMasPinnaRequest request) {

        EntMasPinna entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Pinna not found", 404);
        }

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setPinnaStatus(request.getPinnaStatus());
        entity.setLastUpdatedBy(userContext.getUserFullName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<EntMasPinnaResponse> changeStatus(
            Long id, String status) {

        EntMasPinna entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Pinna not found", 404);
        }

        if (!status.equals("y") &&
                !status.equals("n")) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Invalid status", 400);
        }

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setStatus(status);
        entity.setLastUpdatedBy(userContext.getUserFullName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    private EntMasPinnaResponse toResponse(EntMasPinna e) {
        return new EntMasPinnaResponse(
                e.getId(),
                e.getPinnaStatus(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
