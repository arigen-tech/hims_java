package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasComponentFailureReason;
import com.hims.entity.User;
import com.hims.entity.repository.MasComponentFailureReasonRepository;
import com.hims.request.MasComponentFailureReasonRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasComponentFailureReasonResponse;
import com.hims.service.MasComponentFailureReasonService;
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
public class MasComponentFailureReasonServiceImpl implements MasComponentFailureReasonService {

    private final MasComponentFailureReasonRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasComponentFailureReasonResponse>> getAll(int flag) {
        try {
            List<MasComponentFailureReason> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByFailureReasonNameAsc("y")
                            : repository.findAllByOrderByOrderhdIdAscLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching component failure reason list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to fetch data",
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasComponentFailureReasonResponse> getById(Long id) {
        try {
            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            toResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Component failure reason not found",
                            404
                    ));
        } catch (Exception e) {
            log.error("Error fetching component failure reason by id: {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to fetch record",
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasComponentFailureReasonResponse> create(MasComponentFailureReasonRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        401
                );
            }
            MasComponentFailureReason entity = MasComponentFailureReason.builder()
                    .failureReasonCode(request.getFailureReasonCode())
                    .failureReasonName(request.getFailureReasonName().trim())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(user.getFullName())
                    .lastUpdatedBy(user.getFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error creating component failure reason", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to create record",
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasComponentFailureReasonResponse> update(Long id, MasComponentFailureReasonRequest request) {
        try {
            MasComponentFailureReason entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Component failure reason not found", 404
                );
            }
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        401
                );
            }
            entity.setFailureReasonCode(request.getFailureReasonCode().trim().toUpperCase());
            entity.setFailureReasonName(request.getFailureReasonName().trim());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(
                    toResponse(entity),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error updating component failure reason id: {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to update record",
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasComponentFailureReasonResponse> changeStatus(Long id, String status) {
        try {
            if ( (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n"))) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Only 'y' or 'n' allowed", 400
                );
            }

            MasComponentFailureReason entity = repository.findById(id).orElse(null);
            if (entity == null) {return ResponseUtils.createNotFoundResponse("Component failure reason not found", 404
                );
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found", 401
                );
            }
            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error changing status for component failure reason id: {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to change status",
                    500
            );
        }
    }

    private MasComponentFailureReasonResponse toResponse(MasComponentFailureReason entity) {
        MasComponentFailureReasonResponse response = new MasComponentFailureReasonResponse();
        response.setFailureReasonId(entity.getFailureReasonId());
        response.setFailureReasonCode(entity.getFailureReasonCode());
        response.setFailureReasonName(entity.getFailureReasonName());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setLastUpdateDate(entity.getLastUpdateDate());
        return response;
    }
}