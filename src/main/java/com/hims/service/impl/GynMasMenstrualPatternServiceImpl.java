package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.GynMasMenstrualPattern;
import com.hims.entity.User;
import com.hims.entity.repository.GynMasMenstrualPatternRepository;
import com.hims.request.GynMasMenstrualPatternRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasMenstrualPatternResponse;
import com.hims.service.GynMasMenstrualPatternService;
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
public class GynMasMenstrualPatternServiceImpl
        implements GynMasMenstrualPatternService {

    @Autowired
    private GynMasMenstrualPatternRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<GynMasMenstrualPatternResponse>> getAll(int flag) {
        log.info("Fetching Menstrual Pattern list, flag={}", flag);
        try {
            List<GynMasMenstrualPattern> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByPatternValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Menstrual Pattern list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<GynMasMenstrualPatternResponse> getById(Long id) {
        log.info("Fetching Menstrual Pattern id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Menstrual Pattern not found", 404));
    }

    @Override
    public ApiResponse<GynMasMenstrualPatternResponse> create(
            GynMasMenstrualPatternRequest request) {

        log.info("Creating Menstrual Pattern={}", request.getPatternValue());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            GynMasMenstrualPattern entity = GynMasMenstrualPattern.builder()
                    .patternValue(request.getPatternValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Menstrual Pattern", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<GynMasMenstrualPatternResponse> update(
            Long id, GynMasMenstrualPatternRequest request) {

        log.info("Updating Menstrual Pattern id={}", id);
        GynMasMenstrualPattern entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Menstrual Pattern not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setPatternValue(request.getPatternValue());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<GynMasMenstrualPatternResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Menstrual Pattern status id={}, status={}", id, status);
        GynMasMenstrualPattern entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Menstrual Pattern not found", 404);
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

    private GynMasMenstrualPatternResponse toResponse(
            GynMasMenstrualPattern e) {

        return new GynMasMenstrualPatternResponse(
                e.getId(),
                e.getPatternValue(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
