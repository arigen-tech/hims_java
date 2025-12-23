package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasPvMembrane;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasPvMembraneRepository;
import com.hims.request.ObMasPvMembraneRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasPvMembraneResponse;
import com.hims.service.ObMasPvMembraneService;
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
public class ObMasPvMembraneServiceImpl implements ObMasPvMembraneService {

    @Autowired
    private ObMasPvMembraneRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasPvMembraneResponse>> getAll(int flag) {
        log.info("Fetching PV Membrane list, flag={}", flag);
        try {
            List<ObMasPvMembrane> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByMembraneStatusAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching PV Membrane list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasPvMembraneResponse> getById(Long id) {
        log.info("Fetching PV Membrane id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "PV Membrane not found", 404));
    }

    @Override
    public ApiResponse<ObMasPvMembraneResponse> create(
            ObMasPvMembraneRequest request) {

        log.info("Creating PV Membrane={}", request.getMembraneStatus());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            ObMasPvMembrane entity = ObMasPvMembrane.builder()
                    .membraneStatus(request.getMembraneStatus())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating PV Membrane", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasPvMembraneResponse> update(
            Long id, ObMasPvMembraneRequest request) {

        log.info("Updating PV Membrane id={}", id);
        ObMasPvMembrane entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "PV Membrane not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setMembraneStatus(request.getMembraneStatus());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasPvMembraneResponse> changeStatus(
            Long id, String status) {

        log.info("Changing PV Membrane status id={}, status={}", id, status);
        ObMasPvMembrane entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "PV Membrane not found", 404);
        }

        if (!status.equals("y")
                && !status.equals("n")) {
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

        entity.setStatus(status.toLowerCase());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    private ObMasPvMembraneResponse toResponse(ObMasPvMembrane e) {
        return new ObMasPvMembraneResponse(
                e.getId(),
                e.getMembraneStatus(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
