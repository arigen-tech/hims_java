package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.OpthMasLensType;
import com.hims.entity.User;
import com.hims.entity.repository.OpthMasLensTypeRepository;
import com.hims.request.OpthMasLensTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpthMasLensTypeResponse;
import com.hims.service.OpthMasLensTypeService;
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
public class OpthMasLensTypeServiceImpl implements OpthMasLensTypeService {

    @Autowired
    private OpthMasLensTypeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<OpthMasLensTypeResponse>> getAll(int flag) {
        log.info("Fetching Lens Type list, flag={}", flag);
        try {
            List<OpthMasLensType> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByLensTypeAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching Lens Type list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasLensTypeResponse> getById(Long id) {
        log.info("Fetching Lens Type id={}", id);
        try {
            OpthMasLensType lensType =
                    repository.findById(id).orElse(null);

            if (lensType == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Lens Type not found", 404);
            }

            return ResponseUtils.createSuccessResponse(
                    toResponse(lensType), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Lens Type", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error occurred", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasLensTypeResponse> create(
            OpthMasLensTypeRequest request) {

        log.info("Creating Lens Type={}", request.getLensType());
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            OpthMasLensType lensType = OpthMasLensType.builder()
                    .lensType(request.getLensType())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(lensType);

            return ResponseUtils.createSuccessResponse(
                    toResponse(lensType), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Lens Type", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasLensTypeResponse> update(
            Long id, OpthMasLensTypeRequest request) {

        log.info("Updating Lens Type id={}", id);
        try {
            OpthMasLensType lensType =
                    repository.findById(id).orElse(null);

            if (lensType == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Lens Type not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            lensType.setLensType(request.getLensType());
            lensType.setLastUpdatedBy(user.getFirstName());
            lensType.setLastUpdateDate(LocalDateTime.now());

            repository.save(lensType);

            return ResponseUtils.createSuccessResponse(
                    toResponse(lensType), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Lens Type", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500
            );
        }
    }

    @Override
    public ApiResponse<OpthMasLensTypeResponse> changeStatus(
            Long id, String status) {

        log.info("Changing Lens Type status id={}, status={}", id, status);
        try {
            OpthMasLensType lensType =
                    repository.findById(id).orElse(null);

            if (lensType == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Lens Type not found", 404);
            }

            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400
                );
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404
                );
            }

            lensType.setStatus(status);
            lensType.setLastUpdatedBy(user.getFirstName());
            lensType.setLastUpdateDate(LocalDateTime.now());

            repository.save(lensType);

            return ResponseUtils.createSuccessResponse(
                    toResponse(lensType), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Lens Type status", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500
            );
        }
    }

    private OpthMasLensTypeResponse toResponse(OpthMasLensType l) {
        return new OpthMasLensTypeResponse(
                l.getId(),
                l.getLensType(),
                l.getStatus(),
                l.getLastUpdateDate()
        );
    }
}
