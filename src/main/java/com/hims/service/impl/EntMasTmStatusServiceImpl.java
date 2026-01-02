package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.EntMasTmStatus;
import com.hims.entity.User;
import com.hims.entity.repository.EntMasTmStatusRepository;
import com.hims.request.EntMasTmStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasTmStatusResponse;
import com.hims.service.EntMasTmStatusService;
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
public class EntMasTmStatusServiceImpl
        implements EntMasTmStatusService {

    @Autowired
    private EntMasTmStatusRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<EntMasTmStatusResponse>> getAll(int flag) {
        log.info("Fetching TM Status list, flag={}", flag);
        try {
            List<EntMasTmStatus> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByTmStatusAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching TM Status list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasTmStatusResponse> getById(Long id) {
        log.info("Fetching TM Status by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "TM Status not found", 404));
        } catch (Exception e) {
            log.error("Error fetching TM Status by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasTmStatusResponse> create(
            EntMasTmStatusRequest request) {
        log.info("Creating TM Status");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            EntMasTmStatus entity = EntMasTmStatus.builder()
                    .tmStatus(request.getTmStatus())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating TM Status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasTmStatusResponse> update(
            Long id, EntMasTmStatusRequest request) {
        log.info("Updating TM Status id={}", id);
        try {
            EntMasTmStatus entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "TM Status not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setTmStatus(request.getTmStatus());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating TM Status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasTmStatusResponse> changeStatus(
            Long id, String status) {
        log.info("Changing TM Status id={}, status={}", id, status);
        try {
            EntMasTmStatus entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "TM Status not found", 404);
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
        } catch (Exception e) {
            log.error("Error changing TM Status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private EntMasTmStatusResponse toResponse(EntMasTmStatus e) {
        return new EntMasTmStatusResponse(
                e.getId(),
                e.getTmStatus(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
