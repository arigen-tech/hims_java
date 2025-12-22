package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.ObMasPresentation;
import com.hims.entity.User;
import com.hims.entity.repository.ObMasPresentationRepository;
import com.hims.request.ObMasPresentationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasPresentationResponse;
import com.hims.service.ObMasPresentationService;
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
public class ObMasPresentationServiceImpl
        implements ObMasPresentationService {

    @Autowired
    private ObMasPresentationRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<ObMasPresentationResponse>> getAll(int flag) {
        log.info("Fetching Presentation list, flag={}", flag);
        try {
            List<ObMasPresentation> list =
                    (flag == 1)
                            ? repository
                            .findByStatusIgnoreCaseOrderByPresentationValueAsc("y")
                            : repository
                            .findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Presentation list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<ObMasPresentationResponse> getById(Long id) {
        log.info("Fetching Presentation id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Presentation not found", 404));
    }

    @Override
    public ApiResponse<ObMasPresentationResponse> create(
            ObMasPresentationRequest request) {

        log.info("Creating Presentation={}", request.getPresentationValue());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }


            ObMasPresentation entity = ObMasPresentation.builder()
                    .presentationValue(request.getPresentationValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Presentation", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<ObMasPresentationResponse> update(
            Long id, ObMasPresentationRequest request) {

        log.info("Updating Presentation id={}", id);
        ObMasPresentation entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Presentation not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404
            );
        }

        entity.setPresentationValue(request.getPresentationValue());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<ObMasPresentationResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Presentation status id={}, status={}", id, status);
        ObMasPresentation entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Presentation not found", 404);
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

    private ObMasPresentationResponse toResponse(
            ObMasPresentation e) {

        return new ObMasPresentationResponse(
                e.getId(),
                e.getPresentationValue(),
                e.getStatus(),
                e.getLastUpdateDate()
        );
    }
}
