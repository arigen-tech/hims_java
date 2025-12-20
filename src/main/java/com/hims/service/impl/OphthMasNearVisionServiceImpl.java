package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.OphthMasNearVision;
import com.hims.entity.User;
import com.hims.entity.repository.OphthMasNearVisionRepository;
import com.hims.request.OphthMasNearVisionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OphthMasNearVisionResponse;
import com.hims.service.OphthMasNearVisionService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class OphthMasNearVisionServiceImpl implements OphthMasNearVisionService {

    @Autowired
    private OphthMasNearVisionRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<OphthMasNearVisionResponse> create(
            OphthMasNearVisionRequest request) {

        try {
            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            OphthMasNearVision entity = OphthMasNearVision.builder()
                    .nearValue(request.getNearValue())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error saving Near Vision", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Save failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OphthMasNearVisionResponse> update(
            Long id, OphthMasNearVisionRequest request) {

        try {
            OphthMasNearVision entity =
                    repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Record not found", 404
                );
            }

            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            entity.setNearValue(request.getNearValue());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error updating Near Vision", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }


    @Override
    public ApiResponse<OphthMasNearVisionResponse> getById(Long id) {

        try {
            OphthMasNearVision entity =
                    repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Record not found", 404
                );
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching Near Vision by id", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Fetch failed", 500
            );
        }
    }

    // 4
    @Override
    public ApiResponse<List<OphthMasNearVisionResponse>> getAll(int flag) {

        try {
            List<OphthMasNearVision> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByNearValueAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            List<OphthMasNearVisionResponse> response =
                    list.stream().map(this::toResponse).toList();

            return ResponseUtils.createSuccessResponse(
                    response, new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching Near Vision list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Fetch list failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OphthMasNearVisionResponse> changeStatus(
            Long id, String status) {

        try {
            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status!", 400
                );
            }

            OphthMasNearVision entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Record not found", 404
                );
            }

            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            entity.setStatus(status);
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error changing status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status change failed", 500
            );
        }
    }

    private OphthMasNearVisionResponse toResponse(
            OphthMasNearVision entity) {

        return OphthMasNearVisionResponse.builder()
                .id(entity.getId())
                .nearValue(entity.getNearValue())
                .status(entity.getStatus())
                .lastUpdateDate(entity.getLastUpdateDate())
                .build();
    }
}
