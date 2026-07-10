package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasQuestion;
import com.hims.entity.MasQuestionOptionValue;
import com.hims.entity.OpdQuestionMaster;
import com.hims.entity.User;
import com.hims.entity.repository.MasQuestionOptionValueRepository;
import com.hims.entity.repository.MasQuestionRepository;
import com.hims.entity.repository.OpdQuestionMasterRepository;
import com.hims.request.MasQuestionOptionValueRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasQuestionOptionValueResponse;
import com.hims.service.MasQuestionOptionValueService;
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
public class MasQuestionOptionValueServiceImpl implements MasQuestionOptionValueService {

    private final MasQuestionOptionValueRepository repository;

    private final AuthUtil authUtil;
    private final OpdQuestionMasterRepository opdQuestionMasterRepository;

    @Override
    public ApiResponse<List<MasQuestionOptionValueResponse>> getAll(int flag) {
        log.info("Fetching Question Option Value list, flag={}", flag);
        try {
            List<MasQuestionOptionValue> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByOptionValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Question Option Value list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionOptionValueResponse> getById(Long id) {
        log.info("Fetching Question Option Value by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Question option value not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Question Option Value by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionOptionValueResponse> create(MasQuestionOptionValueRequest request) {
        log.info("Creating Question Option Value");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            Optional<OpdQuestionMaster> opdQuestionMaster = opdQuestionMasterRepository.findById(request.getQuestionId());
            if (opdQuestionMaster.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Question Id", 400);
            }


            MasQuestionOptionValue entity = MasQuestionOptionValue.builder()
                    .optionCode(request.getOptionCode().trim())
                    .optionValue(request.getOptionValue().trim())
                    .optionScore(request.getOptionScore())
                    .questionId(opdQuestionMaster.get())
                    .status(AppConstants.STATUS_Y.toLowerCase())
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Question Option Value", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionOptionValueResponse> update(Long id, MasQuestionOptionValueRequest request) {
        log.info("Updating Question Option Value id={}", id);
        try {
            MasQuestionOptionValue entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Question option value not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            Optional<OpdQuestionMaster> masQuestion = opdQuestionMasterRepository.findById(request.getQuestionId());
            if (masQuestion.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Question Id", 400);
            }
            entity.setOptionCode(request.getOptionCode());
            entity.setOptionValue(request.getOptionValue());
            entity.setOptionScore(request.getOptionScore());
            entity.setQuestionId(masQuestion.get());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Question Option Value id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionOptionValueResponse> changeStatus(Long id, String status) {
        log.info("Changing Question Option Value status id={}, status={}", id, status);
        try {
            MasQuestionOptionValue entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Question option value not found", 404);
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
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing Question Option Value status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasQuestionOptionValueResponse toResponse(MasQuestionOptionValue e) {
        MasQuestionOptionValueResponse response = new MasQuestionOptionValueResponse();
        response.setId(e.getId());
        response.setOptionCode(e.getOptionCode());
        response.setOptionValue(e.getOptionValue());
        response.setOptionScore(e.getOptionScore());
        response.setStatus(e.getStatus());
        response.setQuestionId(e.getQuestionId() != null ? e.getQuestionId().getId() : null);
        response.setQuestionName(e.getQuestionId() != null ? e.getQuestionId().getQuestion() : null);
        response.setLastUpdateDate(e.getLastUpdateDate());
        response.setCreatedBy(e.getCreatedBy());
        response.setLastUpdatedBy(e.getLastUpdatedBy());
        return response;
    }
}