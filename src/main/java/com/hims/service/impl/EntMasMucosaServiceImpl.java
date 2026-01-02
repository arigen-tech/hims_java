package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.EntMasMucosa;
import com.hims.entity.User;
import com.hims.entity.repository.EntMasMucosaRepository;
import com.hims.request.EntMasMucosaRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasMucosaResponse;
import com.hims.service.EntMasMucosaService;
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
public class EntMasMucosaServiceImpl
        implements EntMasMucosaService {

    @Autowired
    private EntMasMucosaRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<EntMasMucosaResponse>> getAll(int flag) {
        log.info("Fetching Mucosa list, flag={}", flag);
        try {
            List<EntMasMucosa> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByMucosaStatusAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Mucosa list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasMucosaResponse> getById(Long id) {
        log.info("Fetching Mucosa by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Mucosa not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Mucosa by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasMucosaResponse> create(
            EntMasMucosaRequest request) {
        log.info("Creating Mucosa");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            EntMasMucosa entity = EntMasMucosa.builder()
                    .mucosaStatus(request.getMucosaStatus())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Mucosa", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasMucosaResponse> update(
            Long id, EntMasMucosaRequest request) {
        log.info("Updating Mucosa id={}", id);
        try {
            EntMasMucosa entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Mucosa not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setMucosaStatus(request.getMucosaStatus());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Mucosa id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasMucosaResponse> changeStatus(
            Long id, String status) {
        log.info("Changing Mucosa status, id={}, status={}", id, status);
        try {
            EntMasMucosa entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Mucosa not found", 404);
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
            log.error("Error changing Mucosa status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private EntMasMucosaResponse toResponse(EntMasMucosa e) {
        return new EntMasMucosaResponse(
                e.getId(),
                e.getMucosaStatus(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
