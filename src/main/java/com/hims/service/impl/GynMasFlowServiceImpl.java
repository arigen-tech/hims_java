package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.GynMasFlow;
import com.hims.entity.User;
import com.hims.entity.repository.GynMasFlowRepository;
import com.hims.request.GynMasFlowRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasFlowResponse;
import com.hims.service.GynMasFlowService;
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
public class GynMasFlowServiceImpl
        implements GynMasFlowService {

    @Autowired
    private GynMasFlowRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<GynMasFlowResponse>> getAll(int flag) {
        log.info("Fetching Gyn Flow list, flag={}", flag);
        try {
            List<GynMasFlow> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByFlowValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Gyn Flow list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<GynMasFlowResponse> getById(Long id) {
        log.info("Fetching Gyn Flow id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Flow not found", 404));
    }

    @Override
    public ApiResponse<GynMasFlowResponse> create(
            GynMasFlowRequest request) {

        log.info("Creating Gyn Flow={}", request.getFlowValue());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            GynMasFlow entity = GynMasFlow.builder()
                    .flowValue(request.getFlowValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Gyn Flow", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<GynMasFlowResponse> update(
            Long id, GynMasFlowRequest request) {

        log.info("Updating Gyn Flow id={}", id);
        GynMasFlow entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Flow not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setFlowValue(request.getFlowValue());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<GynMasFlowResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Gyn Flow status id={}, status={}", id, status);
        GynMasFlow entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Flow not found", 404);
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

    private GynMasFlowResponse toResponse(
            GynMasFlow e) {

        return new GynMasFlowResponse(
                e.getId(),
                e.getFlowValue(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
