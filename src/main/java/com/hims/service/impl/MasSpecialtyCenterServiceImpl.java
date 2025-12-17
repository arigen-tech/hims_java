package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasSpecialtyCenter;
import com.hims.entity.User;
import com.hims.entity.repository.MasSpecialtyCenterRepository;
import com.hims.request.MasSpecialtyCenterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSpecialtyCenterResponse;
import com.hims.service.MasSpecialtyCenterService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MasSpecialtyCenterServiceImpl implements MasSpecialtyCenterService {


    @Autowired
    private MasSpecialtyCenterRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasSpecialtyCenterResponse>> getAll(int flag) {
        try {
            List<MasSpecialtyCenter> list =
                    (flag == 1)
                            ? repository
                            .findByStatusIgnoreCaseOrderByCenterNameAsc("y")
                            : repository
                            .findAllByOrderByLastUpdateDateDesc();

            List<MasSpecialtyCenterResponse> response =
                    list.stream().map(this::toResponse).toList();

            return ResponseUtils.createSuccessResponse(
                    response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasSpecialtyCenterResponse> getById(Long id) {
        try {
            MasSpecialtyCenter center =
                    repository.findById(id).orElse(null);

            if (center == null)
                return ResponseUtils.createNotFoundResponse(
                        "Center ID not found!", 404);

            return ResponseUtils.createSuccessResponse(
                    toResponse(center), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error fetching record: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasSpecialtyCenterResponse> create(
            MasSpecialtyCenterRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasSpecialtyCenter center =
                    MasSpecialtyCenter.builder()
                            .centerName(request.getCenterName())
                            .description(request.getDescription())
                            .status("y")
                            .createdBy(user.getFirstName())
                            .lastUpdatedBy(user.getFirstName())
                            .lastUpdateDate(LocalDateTime.now())
                            .build();

            MasSpecialtyCenter saved = repository.save(center);

            return ResponseUtils.createSuccessResponse(
                    toResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Creation failed: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasSpecialtyCenterResponse> update(
            Long id, MasSpecialtyCenterRequest request) {
        try {
            MasSpecialtyCenter center =
                    repository.findById(id).orElse(null);

            if (center == null)
                return ResponseUtils.createNotFoundResponse(
                        "Center ID not found!", 404);

            User user = authUtil.getCurrentUser();

            center.setCenterName(request.getCenterName());
            center.setDescription(request.getDescription());
            center.setLastUpdatedBy(user.getFirstName());
            center.setLastUpdateDate(LocalDateTime.now());

            repository.save(center);

            return ResponseUtils.createSuccessResponse(
                    toResponse(center), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Update failed: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasSpecialtyCenterResponse> changeStatus(
            Long id, String status) {
        try {
            MasSpecialtyCenter center =
                    repository.findById(id).orElse(null);

            if (center == null)
                return ResponseUtils.createNotFoundResponse(
                        "Center ID not found!", 404);

            if (!status.equalsIgnoreCase("y")
                    && !status.equalsIgnoreCase("n"))
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status value!",
                        400
                );

            User user = authUtil.getCurrentUser();

            center.setStatus(status);
            center.setLastUpdatedBy(user.getFirstName());
            center.setLastUpdateDate(LocalDateTime.now());

            repository.save(center);

            return ResponseUtils.createSuccessResponse(
                    toResponse(center), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Status update failed: " + e.getMessage(),
                    500
            );
        }
    }

    private MasSpecialtyCenterResponse toResponse(
            MasSpecialtyCenter c) {

        return new MasSpecialtyCenterResponse(
                c.getCenterId(),
                c.getCenterName(),
                c.getDescription(),
                c.getStatus(),
                c.getCreatedBy(),
                c.getLastUpdatedBy(),
                c.getLastUpdateDate()
        );
    }
}
