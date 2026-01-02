package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.EntMasPinna;
import com.hims.entity.User;
import com.hims.entity.repository.EntMasPinnaRepository;
import com.hims.request.EntMasPinnaRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasPinnaResponse;
import com.hims.service.EntMasPinnaService;
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

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        EntMasPinna entity = EntMasPinna.builder()
                .pinnaStatus(request.getPinnaStatus())
                .status("y")
                .createdBy(user.getFirstName())
                .lastUpdatedBy(user.getFirstName())
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

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setPinnaStatus(request.getPinnaStatus());
        entity.setLastUpdatedBy(user.getFirstName());
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

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setStatus(status);
        entity.setLastUpdatedBy(user.getFirstName());
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
