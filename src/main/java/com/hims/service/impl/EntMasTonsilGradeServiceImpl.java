package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.EntMasTonsilGrade;
import com.hims.entity.User;
import com.hims.entity.repository.EntMasTonsilGradeRepository;
import com.hims.request.EntMasTonsilGradeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasTonsilGradeResponse;
import com.hims.service.EntMasTonsilGradeService;
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
public class EntMasTonsilGradeServiceImpl
        implements EntMasTonsilGradeService {

    @Autowired
    private EntMasTonsilGradeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<EntMasTonsilGradeResponse>> getAll(int flag) {
        log.info("Fetching Tonsil Grade list, flag={}", flag);
        try {
            List<EntMasTonsilGrade> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByTonsilGradeAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Tonsil Grade list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasTonsilGradeResponse> getById(Long id) {
        log.info("Fetching Tonsil Grade by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Tonsil Grade not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Tonsil Grade by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<EntMasTonsilGradeResponse> create(
            EntMasTonsilGradeRequest request) {
        log.info("Creating Tonsil Grade");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            EntMasTonsilGrade entity = EntMasTonsilGrade.builder()
                    .tonsilGrade(request.getTonsilGrade())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Tonsil Grade", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasTonsilGradeResponse> update(
            Long id, EntMasTonsilGradeRequest request) {
        log.info("Updating Tonsil Grade id={}", id);
        try {
            EntMasTonsilGrade entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Tonsil Grade not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setTonsilGrade(request.getTonsilGrade());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Tonsil Grade id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<EntMasTonsilGradeResponse> changeStatus(
            Long id, String status) {
        log.info("Changing Tonsil Grade status, id={}, status={}", id, status);
        try {
            EntMasTonsilGrade entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Tonsil Grade not found", 404);
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
            log.error("Error changing Tonsil Grade status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private EntMasTonsilGradeResponse toResponse(EntMasTonsilGrade e) {
        return new EntMasTonsilGradeResponse(
                e.getId(),
                e.getTonsilGrade(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
