package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasStationPresenting;
import com.hims.entity.repository.ObMasStationPresentingRepository;
import com.hims.request.ObMasStationPresentingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasStationPresentingResponse;
import com.hims.response.UserContext;
import com.hims.service.ObMasStationPresentingService;
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
public class ObMasStationPresentingServiceImpl
        implements ObMasStationPresentingService {

    @Autowired
    private ObMasStationPresentingRepository repository;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasStationPresentingResponse>> getAll(int flag) {
        log.info("Fetching Station Presenting list, flag={}", flag);
        try {
            List<ObMasStationPresenting> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByStationValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Station Presenting list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasStationPresentingResponse> getById(Long id) {
        log.info("Fetching Station Presenting id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Station Presenting not found", 404));
    }

    @Override
    public ApiResponse<ObMasStationPresentingResponse> create(
            ObMasStationPresentingRequest request) {

        log.info("Creating Station Presenting={}", request.getStationValue());
        try {
            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            ObMasStationPresenting entity = ObMasStationPresenting.builder()
                    .stationValue(request.getStationValue())
                    .status("y")
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Station Presenting", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasStationPresentingResponse> update(
            Long id, ObMasStationPresentingRequest request) {

        log.info("Updating Station Presenting id={}", id);
        ObMasStationPresenting entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Station Presenting not found", 404);
        }

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setStationValue(request.getStationValue());
        entity.setLastUpdatedBy(userContext.getUserFullName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasStationPresentingResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Station Presenting status id={}, status={}", id, status);
        ObMasStationPresenting entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Station Presenting not found", 404);
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
    }

    private ObMasStationPresentingResponse toResponse(
            ObMasStationPresenting e) {

        return new ObMasStationPresentingResponse(
                e.getId(),
                e.getStationValue(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
