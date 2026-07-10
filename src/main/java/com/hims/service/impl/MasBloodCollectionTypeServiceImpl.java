package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodCollectionType;
import com.hims.entity.User;
import com.hims.entity.repository.MasBloodCollectionTypeRepository;
import com.hims.request.MasBloodCollectionTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodCollectionTypeResponse;
import com.hims.service.MasBloodCollectionTypeService;
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
public class MasBloodCollectionTypeServiceImpl
        implements MasBloodCollectionTypeService {

    private final MasBloodCollectionTypeRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasBloodCollectionTypeResponse>> getAll(int flag) {
        try {
            List<MasBloodCollectionType> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByCollectionTypeNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching collection types, flag={}", flag, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch data", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodCollectionTypeResponse> getById(Long id) {
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Collection type not found", 404));
        } catch (Exception e) {
            log.error("Error fetching collection type by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch data", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodCollectionTypeResponse> create(
            MasBloodCollectionTypeRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            MasBloodCollectionType entity = MasBloodCollectionType.builder()
                    .collectionTypeCode(request.getCollectionTypeCode())
                    .collectionTypeName(request.getCollectionTypeName())
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
            log.error("Error creating collection type, request={}", request, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to create collection type", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodCollectionTypeResponse> update(
            Long id, MasBloodCollectionTypeRequest request) {
        try {
            MasBloodCollectionType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Collection type not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            entity.setCollectionTypeCode(request.getCollectionTypeCode());
            entity.setCollectionTypeName(request.getCollectionTypeName());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating collection type, id={}, request={}", id, request, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to update collection type", 500);
        }
    }

    @Override
    public ApiResponse<MasBloodCollectionTypeResponse> changeStatus(
            Long id, String status) {
        try {
            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);
            }

            MasBloodCollectionType entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Collection type not found", 404);
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
            log.error("Error changing collection type status, id={}, status={}", id, status, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to change status", 500);
        }
    }

    private MasBloodCollectionTypeResponse toResponse(
            MasBloodCollectionType e) {

        MasBloodCollectionTypeResponse r = new MasBloodCollectionTypeResponse();
        r.setCollectionTypeId(e.getCollectionTypeId());
        r.setCollectionTypeCode(e.getCollectionTypeCode());
        r.setCollectionTypeName(e.getCollectionTypeName());
        r.setDescription(e.getDescription());
        r.setStatus(e.getStatus());
        r.setLastUpdateDate(e.getLastUpdateDate());
        r.setCreatedBy(e.getCreatedBy());
        r.setLastUpdatedBy(e.getLastUpdatedBy());
        return r;
    }
}
