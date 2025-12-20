package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.OpthMasDistanceVision;
import com.hims.entity.User;
import com.hims.entity.repository.OpthMasDistanceVisionRepository;
import com.hims.request.OpthMasDistanceVisionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpthMasDistanceVisionResponse;
import com.hims.service.OpthMasDistanceVisionService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OpthMasDistanceVisionServiceImpl implements OpthMasDistanceVisionService {

    @Autowired
    private OpthMasDistanceVisionRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<OpthMasDistanceVisionResponse>> getAll(int flag) {
        log.info("Fetching Distance Vision list, flag={}", flag);
        try {
            List<OpthMasDistanceVision> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByVisionValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching Distance Vision list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasDistanceVisionResponse> getById(Long id) {
        log.info("Fetching Distance Vision by id={}", id);
        try {
            OpthMasDistanceVision vision =
                    repository.findById(id).orElse(null);

            if (vision == null) {
                log.warn("Distance Vision not found for id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Vision ID not found!", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(vision), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Distance Vision id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasDistanceVisionResponse> create(
            OpthMasDistanceVisionRequest request) {

        log.info("Creating Distance Vision value={}", request.getVisionValue());
        try {
            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            OpthMasDistanceVision vision =
                    OpthMasDistanceVision.builder()
                            .visionValue(request.getVisionValue())
                            .status("y")
                            .createdBy(user.getFirstName())
                            .lastUpdatedBy(user.getFirstName())
                            .lastUpdateDate(LocalDateTime.now())
                            .build();

            repository.save(vision);

            log.info("Distance Vision created successfully, id={}", vision.getId());

            return ResponseUtils.createSuccessResponse(
                    toResponse(vision), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Distance Vision", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasDistanceVisionResponse> update(
            Long id, OpthMasDistanceVisionRequest request) {

        log.info("Updating Distance Vision id={}", id);
        try {
            OpthMasDistanceVision vision =
                    repository.findById(id).orElse(null);

            if (vision == null) {
                log.warn("Distance Vision not found for update, id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Vision ID not found!", 404);
            }

            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            vision.setVisionValue(request.getVisionValue());
            vision.setLastUpdatedBy(user.getFirstName());
            vision.setLastUpdateDate(LocalDateTime.now());

            repository.save(vision);

            log.info("Distance Vision updated successfully, id={}", id);

            return ResponseUtils.createSuccessResponse(
                    toResponse(vision), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Distance Vision id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasDistanceVisionResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Distance Vision status, id={}, status={}", id, status);
        try {
            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status!", 400
                );
            }

            OpthMasDistanceVision vision =
                    repository.findById(id).orElse(null);

            if (vision == null) {
                log.warn("Distance Vision not found for status change, id={}", id);
                return ResponseUtils.createNotFoundResponse(
                        "Vision ID not found!", 404);
            }

            User user = authUtil.getCurrentUser();

            vision.setStatus(status);
            vision.setLastUpdatedBy(user.getFirstName());
            vision.setLastUpdateDate(LocalDateTime.now());

            repository.save(vision);

            log.info("Distance Vision status updated successfully, id={}", id);

            return ResponseUtils.createSuccessResponse(
                    toResponse(vision), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Distance Vision status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private OpthMasDistanceVisionResponse toResponse(
            OpthMasDistanceVision v) {

        return new OpthMasDistanceVisionResponse(
                v.getId(),
                v.getVisionValue(),
                v.getStatus(),
                v.getLastUpdateDate()
        );
    }
}
