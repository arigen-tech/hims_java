package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodInventoryStatus;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodInventoryStatusRepository;
import com.hims.request.MasBloodInventoryStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodInventoryStatusResponse;
import com.hims.service.MasBloodInventoryStatusService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class MasBloodInventoryStatusServiceImpl
        implements MasBloodInventoryStatusService {

    private final MasBloodInventoryStatusRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasBloodInventoryStatusResponse>> getAll(int flag) {
        try {
            List<MasBloodInventoryStatus> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByStatusCodeAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching inventory status list, flag={}", flag, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch data", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodInventoryStatusResponse> getById(Long id) {
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Inventory status not found", 404));
        } catch (Exception e) {
            log.error("Error fetching inventory status by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch data", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodInventoryStatusResponse> create(
            MasBloodInventoryStatusRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            MasBloodInventoryStatus entity = MasBloodInventoryStatus.builder()
                    .statusCode(request.getStatusCode().toUpperCase())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating inventory status, request={}", request, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to create inventory status", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodInventoryStatusResponse> update(
            Long id, MasBloodInventoryStatusRequest request) {
        try {
            MasBloodInventoryStatus entity =
                    repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Inventory status not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            entity.setStatusCode(request.getStatusCode().toUpperCase());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating inventory status, id={}, request={}", id, request, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to update inventory status", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodInventoryStatusResponse> changeStatus(
            Long id, String status) {
        try {
            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);
            }

            MasBloodInventoryStatus entity =
                    repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Inventory status not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing inventory status, id={}, status={}", id, status, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to change status", 500);
        }
    }

    private MasBloodInventoryStatusResponse toResponse(
            MasBloodInventoryStatus e) {

        MasBloodInventoryStatusResponse r =
                new MasBloodInventoryStatusResponse();

        r.setInventoryStatusId(e.getInventoryStatusId());
        r.setStatusCode(e.getStatusCode());
        r.setDescription(e.getDescription());
        r.setStatus(e.getStatus());
        r.setLastUpdateDate(e.getLastUpdateDate());
        r.setCreatedBy(e.getCreatedBy());
        r.setLastUpdatedBy(e.getLastUpdatedBy());
        return r;
    }
}
