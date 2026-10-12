package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodUnitStatus;
import com.hims.entity.repository.MasBloodUnitStatusRepository;
import com.hims.request.MasBloodUnitStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodUnitStatusResponse;
import com.hims.response.UserContext;
import com.hims.service.MasBloodUnitStatusService;
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
public class MasBloodUnitStatusServiceImpl
        implements MasBloodUnitStatusService {

    private final MasBloodUnitStatusRepository repository;
    private final AuthUtil authUtil;
    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<MasBloodUnitStatusResponse>> getAll(int flag) {
        try {
            List<MasBloodUnitStatus> list =
                    (flag == 1)
                            ? repository
                            .findByStatusIgnoreCaseOrderByStatusNameAsc("y")
                            : repository
                            .findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching blood unit status list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch data", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodUnitStatusResponse> getById(Long id) {
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Blood unit status not found", 404));
        } catch (Exception e) {
            log.error("Error fetching blood unit status by id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch record", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodUnitStatusResponse> create(
            MasBloodUnitStatusRequest request) {
        try {
            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            MasBloodUnitStatus entity = MasBloodUnitStatus.builder()
                    .statusCode(request.getStatusCode().toUpperCase())
                    .statusName(request.getStatusName())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating blood unit status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to create record", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodUnitStatusResponse> update(
            Long id, MasBloodUnitStatusRequest request) {
        try {
            MasBloodUnitStatus entity =
                    repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood unit status not found", 404);
            }

            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            entity.setStatusCode(request.getStatusCode().toUpperCase());
            entity.setStatusName(request.getStatusName());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(userContext.getUserFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating blood unit status id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to update record", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodUnitStatusResponse> changeStatus(
            Long id, String status) {
        try {
            if (!status.equals("y") && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);
            }

            MasBloodUnitStatus entity =
                    repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Blood unit status not found", 404);
            }

            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            entity.setStatus(status);
            entity.setLastUpdatedBy(userContext.getUserFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing status for blood unit status id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to change status", 500);
        }
    }

    private MasBloodUnitStatusResponse toResponse(
            MasBloodUnitStatus e) {

        MasBloodUnitStatusResponse r =
                new MasBloodUnitStatusResponse();

        r.setStatusId(e.getStatusId());
        r.setStatusCode(e.getStatusCode());
        r.setStatusName(e.getStatusName());
        r.setDescription(e.getDescription());
        r.setStatus(e.getStatus());
        r.setLastUpdateDate(e.getLastUpdateDate());
        r.setCreatedBy(e.getCreatedBy());
        r.setLastUpdatedBy(e.getLastUpdatedBy());
        return r;
    }
}
