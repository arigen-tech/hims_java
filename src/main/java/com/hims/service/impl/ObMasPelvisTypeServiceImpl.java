package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasPelvisType;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasPelvisTypeRepository;
import com.hims.request.ObMasPelvisTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasPelvisTypeResponse;
import com.hims.service.ObMasPelvisTypeService;
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
public class ObMasPelvisTypeServiceImpl
        implements ObMasPelvisTypeService {

    @Autowired
    private ObMasPelvisTypeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasPelvisTypeResponse>> getAll(int flag) {
        log.info("Fetching Pelvis Type list, flag={}", flag);
        try {
            List<ObMasPelvisType> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByPelvisTypeAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Pelvis Type list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasPelvisTypeResponse> getById(Long id) {
        log.info("Fetching Pelvis Type id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Pelvis Type not found", 404));
    }

    @Override
    public ApiResponse<ObMasPelvisTypeResponse> create(
            ObMasPelvisTypeRequest request) {

        log.info("Creating Pelvis Type={}", request.getPelvisType());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            ObMasPelvisType entity = ObMasPelvisType.builder()
                    .pelvisType(request.getPelvisType())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Pelvis Type", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasPelvisTypeResponse> update(
            Long id, ObMasPelvisTypeRequest request) {

        log.info("Updating Pelvis Type id={}", id);
        ObMasPelvisType entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Pelvis Type not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setPelvisType(request.getPelvisType());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasPelvisTypeResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Pelvis Type status id={}, status={}", id, status);
        ObMasPelvisType entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Pelvis Type not found", 404);
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

    private ObMasPelvisTypeResponse toResponse(
            ObMasPelvisType e) {

        return new ObMasPelvisTypeResponse(
                e.getId(),
                e.getPelvisType(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
