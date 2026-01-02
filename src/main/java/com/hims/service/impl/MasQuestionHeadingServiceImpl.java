package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasQuestionHeading;
import com.hims.entity.User;
import com.hims.entity.repository.MasQuestionHeadingRepository;
import com.hims.request.MasQuestionHeadingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasQuestionHeadingResponse;
import com.hims.service.MasQuestionHeadingService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class MasQuestionHeadingServiceImpl
        implements MasQuestionHeadingService {

    private final MasQuestionHeadingRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasQuestionHeadingResponse>> getAll(int flag) {
        log.info("Fetching Question Heading list, flag={}", flag);
        try {
            List<MasQuestionHeading> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByQuestionHeadingNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Question Heading list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionHeadingResponse> getById(Long id) {
        log.info("Fetching Question Heading by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Question Heading not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Question Heading by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionHeadingResponse> create(
            MasQuestionHeadingRequest request) {
        log.info("Creating Question Heading");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            MasQuestionHeading entity = MasQuestionHeading.builder()
                    .questionHeadingCode(request.getQuestionHeadingCode())
                    .questionHeadingName(request.getQuestionHeadingName())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Question Heading", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionHeadingResponse> update(
            Long id, MasQuestionHeadingRequest request) {
        log.info("Updating Question Heading id={}", id);
        try {
            MasQuestionHeading entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Question Heading not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setQuestionHeadingCode(request.getQuestionHeadingCode());
            entity.setQuestionHeadingName(request.getQuestionHeadingName());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Question Heading id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionHeadingResponse> changeStatus(
            Long id, String status) {
        log.info("Changing Question Heading status, id={}, status={}", id, status);
        try {
            MasQuestionHeading entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Question Heading not found", 404);
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
            log.error("Error changing Question Heading status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasQuestionHeadingResponse toResponse(MasQuestionHeading e) {
        return new MasQuestionHeadingResponse(
                e.getQuestionHeadingId(),
                e.getQuestionHeadingCode(),
                e.getQuestionHeadingName(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
