package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.EntMasSeptum;
import com.hims.entity.User;
import com.hims.entity.repository.EntMasSeptumRepository;
import com.hims.request.EntMasSeptumRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasSeptumResponse;
import com.hims.response.UserContext;
import com.hims.service.EntMasSeptumService;
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
public class EntMasSeptumServiceImpl
        implements EntMasSeptumService {

    @Autowired
    private EntMasSeptumRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<EntMasSeptumResponse>> getAll(int flag) {
        log.info("Fetching Septum list, flag={}", flag);
        try {
            List<EntMasSeptum> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderBySeptumStatusAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Septum list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasSeptumResponse> getById(Long id) {
        log.info("Fetching Septum by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Septum not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Septum by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasSeptumResponse> create(
            EntMasSeptumRequest request) {
        log.info("Creating Septum");
        try {
            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            EntMasSeptum entity = EntMasSeptum.builder()
                    .septumStatus(request.getSeptumStatus())
                    .status(AppConstants.STATUS_Y.toLowerCase())
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Septum", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasSeptumResponse> update(
            Long id, EntMasSeptumRequest request) {
        log.info("Updating Septum id={}", id);
        try {
            EntMasSeptum entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Septum not found", 404);
            }

            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setSeptumStatus(request.getSeptumStatus());
            entity.setLastUpdatedBy(userContext.getUserFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Septum id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasSeptumResponse> changeStatus(
            Long id, String status) {
        log.info("Changing Septum status, id={}, status={}", id, status);
        try {
            EntMasSeptum entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Septum not found", 404);
            }

            if (!status.equals("y")
                    && !status.equals("n")) {
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
            log.error("Error changing Septum status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private EntMasSeptumResponse toResponse(EntMasSeptum e) {
        return new EntMasSeptumResponse(
                e.getId(),
                e.getSeptumStatus(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
