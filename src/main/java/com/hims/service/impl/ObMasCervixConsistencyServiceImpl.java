package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasCervixConsistency;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasCervixConsistencyRepository;
import com.hims.request.ObMasCervixConsistencyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasCervixConsistencyResponse;
import com.hims.service.ObMasCervixConsistencyService;
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
public class ObMasCervixConsistencyServiceImpl
        implements ObMasCervixConsistencyService {

    @Autowired
    private ObMasCervixConsistencyRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasCervixConsistencyResponse>> getAll(int flag) {
        log.info("Fetching Cervix Consistency list, flag={}", flag);
        try {
            List<ObMasCervixConsistency> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByCervixConsistencyAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Cervix Consistency list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasCervixConsistencyResponse> getById(Long id) {
        log.info("Fetching Cervix Consistency id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Cervix Consistency not found", 404));
    }

    @Override
    public ApiResponse<ObMasCervixConsistencyResponse> create(
            ObMasCervixConsistencyRequest request) {

        log.info("Creating Cervix Consistency={}", request.getCervixConsistency());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            ObMasCervixConsistency entity = ObMasCervixConsistency.builder()
                    .cervixConsistency(request.getCervixConsistency())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Cervix Consistency", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasCervixConsistencyResponse> update(
            Long id, ObMasCervixConsistencyRequest request) {

        log.info("Updating Cervix Consistency id={}", id);
        ObMasCervixConsistency entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Cervix Consistency not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setCervixConsistency(request.getCervixConsistency());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasCervixConsistencyResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Cervix Consistency status id={}, status={}", id, status);
        ObMasCervixConsistency entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Cervix Consistency not found", 404);
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

    private ObMasCervixConsistencyResponse toResponse(
            ObMasCervixConsistency e) {

        return new ObMasCervixConsistencyResponse(
                e.getId(),
                e.getCervixConsistency(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
