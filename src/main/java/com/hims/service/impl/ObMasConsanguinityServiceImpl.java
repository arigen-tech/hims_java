package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasConsanguinity;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasConsanguinityRepository;
import com.hims.request.ObMasConsanguinityRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasConsanguinityResponse;
import com.hims.service.ObMasConsanguinityService;
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
public class ObMasConsanguinityServiceImpl implements ObMasConsanguinityService {

    @Autowired
    private ObMasConsanguinityRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasConsanguinityResponse>> getAll(int flag) {
        log.info("Fetching Consanguinity list, flag={}", flag);
        try {
            List<ObMasConsanguinity> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByConsanguinityValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching Consanguinity list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<ObMasConsanguinityResponse> getById(Long id) {
        log.info("Fetching Consanguinity id={}", id);
        try {
            ObMasConsanguinity consanguinity =
                    repository.findById(id).orElse(null);

            if (consanguinity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Consanguinity not found", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(consanguinity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Consanguinity", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<ObMasConsanguinityResponse> create(
            ObMasConsanguinityRequest request) {

        log.info("Creating Consanguinity={}", request.getConsanguinityValue());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            ObMasConsanguinity consanguinity = ObMasConsanguinity.builder()
                    .consanguinityValue(request.getConsanguinityValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(consanguinity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(consanguinity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Consanguinity", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<ObMasConsanguinityResponse> update(
            Long id, ObMasConsanguinityRequest request) {

        log.info("Updating Consanguinity id={}", id);
        try {
            ObMasConsanguinity consanguinity =
                    repository.findById(id).orElse(null);

            if (consanguinity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Consanguinity not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            consanguinity.setConsanguinityValue(
                    request.getConsanguinityValue());
            consanguinity.setLastUpdatedBy(user.getFirstName());
            consanguinity.setLastUpdateDate(LocalDateTime.now());

            repository.save(consanguinity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(consanguinity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Consanguinity", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<ObMasConsanguinityResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Consanguinity status id={}, status={}", id, status);
        try {
            ObMasConsanguinity consanguinity =
                    repository.findById(id).orElse(null);

            if (consanguinity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Consanguinity not found", 404);
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

            consanguinity.setStatus(status);
            consanguinity.setLastUpdatedBy(user.getFirstName());
            consanguinity.setLastUpdateDate(LocalDateTime.now());

            repository.save(consanguinity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(consanguinity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Consanguinity status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private ObMasConsanguinityResponse toResponse(
            ObMasConsanguinity c) {

        return new ObMasConsanguinityResponse(
                c.getId(),
                c.getConsanguinityValue(),
                c.getStatus(),
                c.getLastUpdateDate()
        );
    }
}
