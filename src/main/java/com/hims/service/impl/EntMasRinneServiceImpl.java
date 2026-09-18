package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.EntMasRinne;
import com.hims.entity.repository.EntMasRinneRepository;
import com.hims.request.EntMasRinneRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasRinneResponse;
import com.hims.response.UserContext;
import com.hims.service.EntMasRinneService;
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
public class EntMasRinneServiceImpl implements EntMasRinneService {

    @Autowired
    private EntMasRinneRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;


    @Override
    public ApiResponse<List<EntMasRinneResponse>> getAll(int flag) {
        log.info("Fetching Rinne list, flag={}", flag);
        try {
            List<EntMasRinne> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByRinneResultAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Rinne list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasRinneResponse> getById(Long id) {
        log.info("Fetching Rinne by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Rinne not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Rinne by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasRinneResponse> create(EntMasRinneRequest request) {
        log.info("Creating Rinne");
        try {
            UserContext userContext = userContextService.getCurrentUserContext();

            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            EntMasRinne entity = EntMasRinne.builder()
                    .rinneResult(request.getRinneResult())
                    .status("y")
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Rinne", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasRinneResponse> update(
            Long id, EntMasRinneRequest request) {
        log.info("Updating Rinne id={}", id);
        try {
            EntMasRinne entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Rinne not found", 404);
            }

            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setRinneResult(request.getRinneResult());
            entity.setLastUpdatedBy(userContext.getUserFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Rinne id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasRinneResponse> changeStatus(
            Long id, String status) {
        log.info("Changing Rinne status, id={}, status={}", id, status);
        try {
            EntMasRinne entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Rinne not found", 404);
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y)
                    && !status.equalsIgnoreCase(AppConstants.STATUS_N)) {
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
        } catch (Exception e) {
            log.error("Error changing Rinne status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private EntMasRinneResponse toResponse(EntMasRinne e) {
        return new EntMasRinneResponse(
                e.getId(),
                e.getRinneResult(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
