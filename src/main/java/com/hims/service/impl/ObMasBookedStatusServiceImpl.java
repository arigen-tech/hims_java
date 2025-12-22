package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasBookedStatus;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasBookedStatusRepository;
import com.hims.request.ObMasBookedStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasBookedStatusResponse;
import com.hims.service.ObMasBookedStatusService;
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
public class ObMasBookedStatusServiceImpl
        implements ObMasBookedStatusService {

    @Autowired
    private ObMasBookedStatusRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasBookedStatusResponse>> getAll(int flag) {
        log.info("Fetching Booked Status list, flag={}", flag);
        try {
            List<ObMasBookedStatus> list =
                    (flag == 1)
                            ? repository
                            .findByStatusIgnoreCaseOrderByBookedStatusAsc("y")
                            : repository
                            .findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Booked Status list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasBookedStatusResponse> getById(Long id) {
        log.info("Fetching Booked Status id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Booked Status not found", 404));
    }

    @Override
    public ApiResponse<ObMasBookedStatusResponse> create(
            ObMasBookedStatusRequest request) {

        log.info("Creating Booked Status={}", request.getBookedStatus());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            ObMasBookedStatus entity = ObMasBookedStatus.builder()
                    .bookedStatus(request.getBookedStatus())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Booked Status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasBookedStatusResponse> update(
            Long id, ObMasBookedStatusRequest request) {

        log.info("Updating Booked Status id={}", id);
        ObMasBookedStatus entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Booked Status not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404
            );
        }
        entity.setBookedStatus(request.getBookedStatus());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasBookedStatusResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Booked Status id={}, status={}", id, status);
        ObMasBookedStatus entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Booked Status not found", 404);
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

    private ObMasBookedStatusResponse toResponse(
            ObMasBookedStatus e) {

        return new ObMasBookedStatusResponse(
                e.getId(),
                e.getBookedStatus(),
                e.getStatus(),
                e.getLastUpdateDate()
        );
    }
}
