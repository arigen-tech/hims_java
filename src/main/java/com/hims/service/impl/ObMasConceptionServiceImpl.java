package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasConception;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasConceptionRepository;
import com.hims.request.ObMasConceptionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasConceptionResponse;
import com.hims.service.ObMasConceptionService;
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
public class ObMasConceptionServiceImpl implements ObMasConceptionService {

    @Autowired
    private ObMasConceptionRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasConceptionResponse>> getAll(int flag) {
        log.info("Fetching Conception list, flag={}", flag);
        try {
            List<ObMasConception> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByConceptionTypeAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching Conception list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<ObMasConceptionResponse> getById(Long id) {
        log.info("Fetching Conception id={}", id);
        try {
            ObMasConception conception =
                    repository.findById(id).orElse(null);

            if (conception == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Conception not found", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(conception), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Conception", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<ObMasConceptionResponse> create(
            ObMasConceptionRequest request) {

        log.info("Creating Conception={}", request.getConceptionType());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            ObMasConception conception = ObMasConception.builder()
                    .conceptionType(request.getConceptionType())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(conception);

            return ResponseUtils.createSuccessResponse(
                    toResponse(conception), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Conception", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<ObMasConceptionResponse> update(
            Long id, ObMasConceptionRequest request) {

        log.info("Updating Conception id={}", id);
        try {
            ObMasConception conception =
                    repository.findById(id).orElse(null);

            if (conception == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Conception not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            conception.setConceptionType(request.getConceptionType());
            conception.setLastUpdatedBy(user.getFirstName());
            conception.setLastUpdateDate(LocalDateTime.now());

            repository.save(conception);

            return ResponseUtils.createSuccessResponse(
                    toResponse(conception), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Conception", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<ObMasConceptionResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Conception status id={}, status={}", id, status);
        try {
            ObMasConception conception =
                    repository.findById(id).orElse(null);

            if (conception == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Conception not found", 404);
            }

            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400
                );
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            conception.setStatus(status);
            conception.setLastUpdatedBy(user.getFirstName());
            conception.setLastUpdateDate(LocalDateTime.now());

            repository.save(conception);

            return ResponseUtils.createSuccessResponse(
                    toResponse(conception), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Conception status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private ObMasConceptionResponse toResponse(ObMasConception c) {
        return new ObMasConceptionResponse(
                c.getId(),
                c.getConceptionType(),
                c.getStatus(),
                c.getLastUpdateDate()
        );
    }
}
