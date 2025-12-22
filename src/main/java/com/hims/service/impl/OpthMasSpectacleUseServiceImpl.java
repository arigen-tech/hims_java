package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.OpthMasSpectacleUse;
import com.hims.entity.User;
import com.hims.entity.repository.OpthMasSpectacleUseRepository;
import com.hims.request.OpthMasSpectacleUseRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpthMasSpectacleUseResponse;
import com.hims.service.OpthMasSpectacleUseService;
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
public class OpthMasSpectacleUseServiceImpl implements OpthMasSpectacleUseService {
   @Autowired
    private OpthMasSpectacleUseRepository repository;
   @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<OpthMasSpectacleUseResponse>> getAll(int flag) {
        log.info("Fetching Spectacle Use list, flag={}", flag);
        try {
            List<OpthMasSpectacleUse> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByUseNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching Spectacle Use list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasSpectacleUseResponse> getById(Long id) {
        log.info("Fetching Spectacle Use by id={}", id);
        try {
            OpthMasSpectacleUse use =
                    repository.findById(id).orElse(null);

            if (use == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Spectacle Use not found", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(use), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Spectacle Use", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasSpectacleUseResponse> create(
            OpthMasSpectacleUseRequest request) {

        log.info("Creating Spectacle Use={}", request.getUseName());
        try {
            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            OpthMasSpectacleUse use = OpthMasSpectacleUse.builder()
                    .useName(request.getUseName())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(use);

            return ResponseUtils.createSuccessResponse(
                    toResponse(use), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Spectacle Use", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasSpectacleUseResponse> update(
            Long id, OpthMasSpectacleUseRequest request) {

        log.info("Updating Spectacle Use id={}", id);
        try {
            OpthMasSpectacleUse use =
                    repository.findById(id).orElse(null);

            if (use == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Spectacle Use not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if( user ==null){
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "current user not fount", 404
                );
            }

            use.setUseName(request.getUseName());
            use.setLastUpdatedBy(user.getFirstName());
            use.setLastUpdateDate(LocalDateTime.now());

            repository.save(use);

            return ResponseUtils.createSuccessResponse(
                    toResponse(use), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Spectacle Use", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasSpectacleUseResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Spectacle Use status id={}, status={}", id, status);
        try {
            OpthMasSpectacleUse use =
                    repository.findById(id).orElse(null);

            if (use == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Spectacle Use not found", 404);
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

            use.setStatus(status);
            use.setLastUpdatedBy(user.getFirstName());
            use.setLastUpdateDate(LocalDateTime.now());

            repository.save(use);

            return ResponseUtils.createSuccessResponse(
                    toResponse(use), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Spectacle Use status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private OpthMasSpectacleUseResponse toResponse(
            OpthMasSpectacleUse u) {

        return new OpthMasSpectacleUseResponse(
                u.getId(),
                u.getUseName(),
                u.getStatus(),
                u.getLastUpdateDate()
        );
    }
}
