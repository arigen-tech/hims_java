package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasTrimester;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasTrimesterRepository;
import com.hims.request.ObMasTrimesterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasTrimesterResponse;
import com.hims.service.ObMasTrimesterService;
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
public class ObMasTrimesterServiceImpl
        implements ObMasTrimesterService {

    @Autowired
    private ObMasTrimesterRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasTrimesterResponse>> getAll(int flag) {
        log.info("Fetching Trimester list, flag={}", flag);
        try {
            List<ObMasTrimester> list =
                    (flag == 1)
                            ? repository
                            .findByStatusIgnoreCaseOrderByTrimesterValueAsc("y")
                            : repository
                            .findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Trimester list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasTrimesterResponse> getById(Long id) {
        log.info("Fetching Trimester id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Trimester not found", 404));
    }

    @Override
    public ApiResponse<ObMasTrimesterResponse> create(
            ObMasTrimesterRequest request) {

        log.info("Creating Trimester={}", request.getTrimesterValue());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }


            ObMasTrimester entity = ObMasTrimester.builder()
                    .trimesterValue(request.getTrimesterValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Trimester", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasTrimesterResponse> update(
            Long id, ObMasTrimesterRequest request) {

        log.info("Updating Trimester id={}", id);
        ObMasTrimester entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Trimester not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404
            );
        }

        entity.setTrimesterValue(request.getTrimesterValue());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasTrimesterResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Trimester status id={}, status={}", id, status);
        ObMasTrimester entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Trimester not found", 404);
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
                    "Current user not found", 404
            );
        }

        entity.setStatus(status);
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    private ObMasTrimesterResponse toResponse(
            ObMasTrimester e) {

        return new ObMasTrimesterResponse(
                e.getId(),
                e.getTrimesterValue(),
                e.getStatus(),
                e.getLastUpdateDate()
        );
    }
}
