package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.OpthMasColorVision;
import com.hims.entity.User;
import com.hims.entity.repository.OpthMasColorVisionRepository;
import com.hims.request.OpthMasColorVisionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpthMasColorVisionResponse;
import com.hims.service.OpthMasColorVisionService;
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
public class OpthMasColorVisionServiceImpl
        implements OpthMasColorVisionService {

    private final OpthMasColorVisionRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<OpthMasColorVisionResponse>> getAll(int flag) {
        log.info("Fetching Color Vision list, flag={}", flag);
        try {
            List<OpthMasColorVision> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByColorValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching Color Vision list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasColorVisionResponse> getById(Long id) {
        log.info("Fetching Color Vision by id={}", id);
        try {
            OpthMasColorVision vision =
                    repository.findById(id).orElse(null);

            if (vision == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Color Vision not found", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(vision), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Color Vision", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasColorVisionResponse> create(
            OpthMasColorVisionRequest request) {

        log.info("Creating Color Vision value={}", request.getColorValue());
        try {
            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            OpthMasColorVision vision = OpthMasColorVision.builder()
                    .colorValue(request.getColorValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(vision);

            return ResponseUtils.createSuccessResponse(
                    toResponse(vision), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Color Vision", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasColorVisionResponse> update(
            Long id, OpthMasColorVisionRequest request) {

        log.info("Updating Color Vision id={}", id);
        try {
            OpthMasColorVision vision =
                    repository.findById(id).orElse(null);

            if (vision == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Color Vision not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            vision.setColorValue(request.getColorValue());
            vision.setLastUpdatedBy(user.getFirstName());
            vision.setLastUpdateDate(LocalDateTime.now());

            repository.save(vision);

            return ResponseUtils.createSuccessResponse(
                    toResponse(vision), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Color Vision", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasColorVisionResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Color Vision status id={}, status={}", id, status);
        try {
            OpthMasColorVision vision =
                    repository.findById(id).orElse(null);

            if (vision == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Color Vision not found", 404);
            }

            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400
                );
            }

            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            vision.setStatus(status);
            vision.setLastUpdatedBy(user.getFirstName());
            vision.setLastUpdateDate(LocalDateTime.now());

            repository.save(vision);

            return ResponseUtils.createSuccessResponse(
                    toResponse(vision), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing Color Vision status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private OpthMasColorVisionResponse toResponse(
            OpthMasColorVision v) {

        return new OpthMasColorVisionResponse(
                v.getId(),
                v.getColorValue(),
                v.getStatus(),
                v.getLastUpdateDate()
        );
    }
}
