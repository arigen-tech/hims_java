package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasCervixPosition;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasCervixPositionRepository;
import com.hims.request.ObMasCervixPositionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasCervixPositionResponse;
import com.hims.service.ObMasCervixPositionService;
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
public class ObMasCervixPositionServiceImpl
        implements ObMasCervixPositionService {

    @Autowired
    private ObMasCervixPositionRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasCervixPositionResponse>> getAll(int flag) {
        log.info("Fetching Cervix Position list, flag={}", flag);
        try {
            List<ObMasCervixPosition> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByCervixPositionAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Cervix Position list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasCervixPositionResponse> getById(Long id) {
        log.info("Fetching Cervix Position id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Cervix Position not found", 404));
    }

    @Override
    public ApiResponse<ObMasCervixPositionResponse> create(
            ObMasCervixPositionRequest request) {

        log.info("Creating Cervix Position={}", request.getCervixPosition());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            ObMasCervixPosition entity = ObMasCervixPosition.builder()
                    .cervixPosition(request.getCervixPosition())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Cervix Position", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasCervixPositionResponse> update(
            Long id, ObMasCervixPositionRequest request) {

        log.info("Updating Cervix Position id={}", id);
        ObMasCervixPosition entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Cervix Position not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setCervixPosition(request.getCervixPosition());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasCervixPositionResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Cervix Position status id={}, status={}", id, status);
        ObMasCervixPosition entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Cervix Position not found", 404);
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

    private ObMasCervixPositionResponse toResponse(
            ObMasCervixPosition e) {

        return new ObMasCervixPositionResponse(
                e.getId(),
                e.getCervixPosition(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
