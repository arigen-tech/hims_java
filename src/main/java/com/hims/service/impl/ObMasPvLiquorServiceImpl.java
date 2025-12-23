package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasPvLiquor;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasPvLiquorRepository;
import com.hims.request.ObMasPvLiquorRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasPvLiquorResponse;
import com.hims.service.ObMasPvLiquorService;
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
public class ObMasPvLiquorServiceImpl implements ObMasPvLiquorService {

    @Autowired
    private ObMasPvLiquorRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasPvLiquorResponse>> getAll(int flag) {
        log.info("Fetching PV Liquor list, flag={}", flag);
        try {
            List<ObMasPvLiquor> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByLiquorValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching PV Liquor list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasPvLiquorResponse> getById(Long id) {
        log.info("Fetching PV Liquor id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "PV Liquor not found", 404));
    }

    @Override
    public ApiResponse<ObMasPvLiquorResponse> create(
            ObMasPvLiquorRequest request) {

        log.info("Creating PV Liquor={}", request.getLiquorValue());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            ObMasPvLiquor entity = ObMasPvLiquor.builder()
                    .liquorValue(request.getLiquorValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating PV Liquor", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasPvLiquorResponse> update(
            Long id, ObMasPvLiquorRequest request) {

        log.info("Updating PV Liquor id={}", id);
        ObMasPvLiquor entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "PV Liquor not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setLiquorValue(request.getLiquorValue());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasPvLiquorResponse> changeStatus(
            Long id, String status) {

        log.info("Changing PV Liquor status id={}, status={}", id, status);
        ObMasPvLiquor entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "PV Liquor not found", 404);
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

    private ObMasPvLiquorResponse toResponse(ObMasPvLiquor e) {
        return new ObMasPvLiquorResponse(
                e.getId(),
                e.getLiquorValue(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
