package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasQuestionHeading;
import com.hims.entity.OpdQuestionMaster;
import com.hims.entity.User;
import com.hims.entity.repository.MasQuestionHeadingRepository;
import com.hims.entity.repository.OpdQuestionMasterRepository;
import com.hims.request.OpdQuestionMasterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdQuestionMasterResponse;
import com.hims.service.OpdQuestionMasterService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpdQuestionMasterServiceImpl implements OpdQuestionMasterService {

    private final OpdQuestionMasterRepository repository;
    private final MasQuestionHeadingRepository masQuestionHeadingRepository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<OpdQuestionMasterResponse>> getAll(int flag) {
        log.info("Fetching OPD Question Master list, flag={}", flag);
        try {
            List<OpdQuestionMaster> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByQuestionAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching OPD Question Master list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<OpdQuestionMasterResponse> getById(Long id) {
        log.info("Fetching OPD Question Master by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "OPD Question Master not found", 404));
        } catch (Exception e) {
            log.error("Error fetching OPD Question Master by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<OpdQuestionMasterResponse> create(OpdQuestionMasterRequest request) {
        log.info("Creating OPD Question Master");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            Optional<MasQuestionHeading> masQuestionHeading = masQuestionHeadingRepository.findById(request.getQuestionHeadingId());

            if (masQuestionHeading.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid Question Heading", 400);
            }

            OpdQuestionMaster entity = OpdQuestionMaster.builder()
                    .question(request.getQuestion())
                    .questionHeading(masQuestionHeading.get())
                    .status(AppConstants.STATUS_Y.toLowerCase())
                    .createdBy(user.getFullName())
                    .lastUpdatedBy(user.getFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating OPD Question Master", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<OpdQuestionMasterResponse> update(Long id, OpdQuestionMasterRequest request) {
        log.info("Updating OPD Question Master id={}", id);
        try {
            OpdQuestionMaster entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "OPD Question Master not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            Optional<MasQuestionHeading> masQuestionHeading = masQuestionHeadingRepository.findById(request.getQuestionHeadingId());

            if (masQuestionHeading.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Question Heading", 400);
            }
            entity.setQuestion(request.getQuestion());
            entity.setQuestionHeading(masQuestionHeading.get());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating OPD Question Master id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<OpdQuestionMasterResponse> changeStatus(Long id, String status) {
        log.info("Changing OPD Question Master status, id={}, status={}", id, status);
        try {
            OpdQuestionMaster entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "OPD Question Master not found", 404);
            }

            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
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

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing OPD Question Master status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private OpdQuestionMasterResponse toResponse(OpdQuestionMaster e) {
        OpdQuestionMasterResponse response = new OpdQuestionMasterResponse();
        response.setId(e.getId());
        response.setQuestion(e.getQuestion());
        response.setQuestionHeadingId(
                e.getQuestionHeading() != null ? e.getQuestionHeading().getQuestionHeadingId() : null);
        response.setQuestionHeadingName(
                e.getQuestionHeading() != null ? e.getQuestionHeading().getQuestionHeadingName() : null);
        response.setStatus(e.getStatus());
        response.setLastUpdateDate(e.getLastUpdateDate());
        response.setCreatedBy(e.getCreatedBy());
        response.setLastUpdatedBy(e.getLastUpdatedBy());
        return response;
    }
}