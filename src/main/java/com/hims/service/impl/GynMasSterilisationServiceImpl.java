package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.GynMasSterilisation;
import com.hims.entity.User;
import com.hims.entity.repository.GynMasSterilisationRepository;
import com.hims.request.GynMasSterilisationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasSterilisationResponse;
import com.hims.service.GynMasSterilisationService;
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
public class GynMasSterilisationServiceImpl
        implements GynMasSterilisationService {

    @Autowired
    private GynMasSterilisationRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<GynMasSterilisationResponse>> getAll(int flag) {
        log.info("Fetching Sterilisation list, flag={}", flag);
        try {
            List<GynMasSterilisation> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderBySterilisationTypeAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Sterilisation list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<GynMasSterilisationResponse> getById(Long id) {
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Sterilisation not found", 404));
    }

    @Override
    public ApiResponse<GynMasSterilisationResponse> create(
            GynMasSterilisationRequest request) {

        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            GynMasSterilisation entity = GynMasSterilisation.builder()
                    .sterilisationType(request.getSterilisationType())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Sterilisation", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<GynMasSterilisationResponse> update(
            Long id, GynMasSterilisationRequest request) {

        GynMasSterilisation entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Sterilisation not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setSterilisationType(request.getSterilisationType());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<GynMasSterilisationResponse> changeStatus(
            Long id, String status) {

        GynMasSterilisation entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Sterilisation not found", 404);
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

        entity.setStatus(status);
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    private GynMasSterilisationResponse toResponse(GynMasSterilisation e) {
        return new GynMasSterilisationResponse(
                e.getId(),
                e.getSterilisationType(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
