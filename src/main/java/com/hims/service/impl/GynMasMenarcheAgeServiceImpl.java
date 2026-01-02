package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.GynMasMenarcheAge;
import com.hims.entity.User;
import com.hims.entity.repository.GynMasMenarcheAgeRepository;
import com.hims.request.GynMasMenarcheAgeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.GynMasMenarcheAgeResponse;
import com.hims.service.GynMasMenarcheAgeService;
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
public class GynMasMenarcheAgeServiceImpl
        implements GynMasMenarcheAgeService {

    @Autowired
    private GynMasMenarcheAgeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<GynMasMenarcheAgeResponse>> getAll(int flag) {
        log.info("Fetching Menarche Age list, flag={}", flag);
        try {
            List<GynMasMenarcheAge> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByMenarcheAgeAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Menarche Age list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<GynMasMenarcheAgeResponse> getById(Long id) {
        log.info("Fetching Menarche Age id={}", id);
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(
                        toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(
                        "Menarche Age not found", 404));
    }

    @Override
    public ApiResponse<GynMasMenarcheAgeResponse> create(
            GynMasMenarcheAgeRequest request) {

        log.info("Creating Menarche Age={}", request.getMenarcheAge());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            GynMasMenarcheAge entity = GynMasMenarcheAge.builder()
                    .menarcheAge(request.getMenarcheAge())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Menarche Age", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<GynMasMenarcheAgeResponse> update(
            Long id, GynMasMenarcheAgeRequest request) {

        log.info("Updating Menarche Age id={}", id);
        GynMasMenarcheAge entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Menarche Age not found", 404);
        }

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found", 404);
        }

        entity.setMenarcheAge(request.getMenarcheAge());
        entity.setLastUpdatedBy(user.getFirstName());
        entity.setLastUpdateDate(LocalDateTime.now());

        repository.save(entity);

        return ResponseUtils.createSuccessResponse(
                toResponse(entity), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<GynMasMenarcheAgeResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Menarche Age status id={}, status={}", id, status);
        GynMasMenarcheAge entity = repository.findById(id).orElse(null);

        if (entity == null) {
            return ResponseUtils.createNotFoundResponse(
                    "Menarche Age not found", 404);
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

    private GynMasMenarcheAgeResponse toResponse(
            GynMasMenarcheAge e) {

        return new GynMasMenarcheAgeResponse(
                e.getId(),
                e.getMenarcheAge(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
