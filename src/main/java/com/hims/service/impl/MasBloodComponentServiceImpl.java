package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodComponent;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodComponentRepository;
import com.hims.request.MasBloodComponentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodComponentResponse;
import com.hims.response.UserContext;
import com.hims.service.MasBloodComponentService;
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
public class MasBloodComponentServiceImpl implements MasBloodComponentService {

    private final MasBloodComponentRepository repository;
    private final AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<MasBloodComponentResponse>> getAll(int flag) {
        log.info("Fetching Blood Component list, flag={}", flag);
        try {
            List<MasBloodComponent> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByComponentNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Blood Component list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodComponentResponse> getById(Long id) {
        log.info("Fetching Blood Component by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Blood Component not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Blood Component by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodComponentResponse> create(
            MasBloodComponentRequest request) {

        log.info("Creating Blood Component");
        try {
            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            MasBloodComponent entity = MasBloodComponent.builder()
                    .componentCode(request.getComponentCode())
                    .componentName(request.getComponentName())
                    .description(request.getDescription())
                    .storageTemp(request.getStorageTemp())
                    .shelfLifeDays(request.getShelfLifeDays())
                    .status("y")
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Blood Component", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodComponentResponse> update(
            Long id, MasBloodComponentRequest request) {

        log.info("Updating Blood Component id={}", id);
        try {
            MasBloodComponent entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood Component not found", 404);
            }

            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setComponentCode(request.getComponentCode());
            entity.setComponentName(request.getComponentName());
            entity.setDescription(request.getDescription());
            entity.setStorageTemp(request.getStorageTemp());
            entity.setShelfLifeDays(request.getShelfLifeDays());
            entity.setLastUpdatedBy(userContext.getUserFullName());

            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Blood Component id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodComponentResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Blood Component status id={}, status={}", id, status);
        try {
            MasBloodComponent entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood Component not found", 404);
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
            log.error("Error changing Blood Component status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasBloodComponentResponse toResponse(MasBloodComponent e) {
        MasBloodComponentResponse res = new MasBloodComponentResponse();
        res.setComponentId(e.getComponentId());
        res.setComponentCode(e.getComponentCode());
        res.setComponentName(e.getComponentName());
        res.setDescription(e.getDescription());
        res.setStorageTemp(e.getStorageTemp());
        res.setShelfLifeDays(e.getShelfLifeDays());
        res.setStatus(e.getStatus());
        res.setLastUpdateDate(e.getLastUpdateDate());
        res.setCreatedBy(e.getCreatedBy());
        res.setLastUpdatedBy(e.getLastUpdatedBy());
        return res;
    }
}
