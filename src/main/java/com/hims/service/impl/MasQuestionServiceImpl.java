package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasQuestion;
import com.hims.entity.MasQuestionHeading;
import com.hims.entity.User;
import com.hims.entity.repository.MasQuestionHeadingRepository;
import com.hims.entity.repository.MasQuestionRepository;
import com.hims.request.MasQuestionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasQuestionResponse;
import com.hims.service.MasQuestionService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasQuestionServiceImpl implements MasQuestionService {

    private final MasQuestionRepository repository;
    private final AuthUtil authUtil;
    @Autowired
    private MasQuestionHeadingRepository masQuestionHeadingRepository;

    @Override
    public ApiResponse<List<MasQuestionResponse>> getAll(int flag) {
        log.info("Fetching Question list, flag={}", flag);
        try {
            List<MasQuestion> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByQuestionAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Question list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionResponse> getById(Long id) {
        log.info("Fetching Question by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Question not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Question by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionResponse> create(MasQuestionRequest request) {
        log.info("Creating Question");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }
            Optional<MasQuestionHeading> masQuestionHeading=masQuestionHeadingRepository.findById(request.getQuestionHeadingId());
            if (masQuestionHeading.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Question Heading", 400);
            }

            MasQuestion entity = MasQuestion.builder()
                    .question(request.getQuestion())
                    .questionHeadingId(masQuestionHeading.orElse(null))
                    .optionValue(request.getOptionValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastChgBy(user.getUserId())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Question", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionResponse> update(
            Long id, MasQuestionRequest request) {
        log.info("Updating Question id={}", id);
        try {
            MasQuestion entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Question not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }
            Optional<MasQuestionHeading> masQuestionHeading=masQuestionHeadingRepository.findById(request.getQuestionHeadingId());
            if (masQuestionHeading.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Question Heading", 400);
            }


            entity.setQuestion(request.getQuestion());
            entity.setQuestionHeadingId(masQuestionHeading.orElse(null));
            entity.setOptionValue(request.getOptionValue());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastChgBy(user.getUserId());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Question id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<MasQuestionResponse> changeStatus(
            Long id, String status) {
        log.info("Changing Question status id={}, status={}", id, status);
        try {
            MasQuestion entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Question not found", 404);
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
            entity.setLastChgBy(user.getUserId());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing Question status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasQuestionResponse toResponse(MasQuestion e) {
        MasQuestionResponse response=new MasQuestionResponse();
        response.setId(e.getId());
        response.setQuestion(e.getQuestion());
        response.setQuestionHeadingId(e.getQuestionHeadingId().getQuestionHeadingId());
        response.setQuestionHeadingName(e.getQuestionHeadingId().getQuestionHeadingName());
        response.setStatus(e.getStatus());
        response.setLastUpdateDate(e.getLastUpdateDate());
return response;
    }

}
