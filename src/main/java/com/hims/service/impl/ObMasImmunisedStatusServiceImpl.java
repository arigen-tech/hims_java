package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.ObMasImmunisedStatus;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasImmunisedStatusRepository;
import com.hims.request.ObMasImmunisedStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasImmunisedStatusResponse;
import com.hims.response.UserContext;
import com.hims.service.ObMasImmunisedStatusService;
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
public class ObMasImmunisedStatusServiceImpl
        implements ObMasImmunisedStatusService {

    @Autowired
    private ObMasImmunisedStatusRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<ObMasImmunisedStatusResponse>> getAll(int flag) {
        log.info("Fetching Immunised Status list, flag={}", flag);
        try {
            List<ObMasImmunisedStatus> list =
                    (flag == 1)
                            ? repository
                            .findByStatusIgnoreCaseOrderByImmunisationValueAsc("y")
                            : repository
                            .findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Immunised Status list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasImmunisedStatusResponse> getById(Long id) {
        log.info("Fetching Immunised Status id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Immunised Status not found", 404));
    }

    @Override
    public ApiResponse<ObMasImmunisedStatusResponse> create(
            ObMasImmunisedStatusRequest request) {

        log.info("Creating Immunised Status={}", request.getImmunisationValue());
        try {
            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            ObMasImmunisedStatus entity = ObMasImmunisedStatus.builder()
                    .immunisationValue(request.getImmunisationValue())
                    .status(AppConstants.STATUS_Y.toLowerCase())
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Immunised Status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasImmunisedStatusResponse> update(
            Long id, ObMasImmunisedStatusRequest request) {

        log.info("Updating Immunised Status id={}", id);
        ObMasImmunisedStatus entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Immunised Status not found", 404);
        }

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404
            );
        }
        entity.setImmunisationValue(request.getImmunisationValue());
        entity.setLastUpdatedBy(userContext.getUserFullName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasImmunisedStatusResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Immunised Status id={}, status={}", id, status);
        ObMasImmunisedStatus entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Immunised Status not found", 404);
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
                    "Current user not found", 404
            );
        }
        entity.setStatus(status);
        entity.setLastUpdatedBy(userContext.getUserFullName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    private ObMasImmunisedStatusResponse toResponse(
            ObMasImmunisedStatus e) {

        return new ObMasImmunisedStatusResponse(
                e.getId(),
                e.getImmunisationValue(),
                e.getStatus(),
                e.getLastUpdateDate()
        );
    }
}
