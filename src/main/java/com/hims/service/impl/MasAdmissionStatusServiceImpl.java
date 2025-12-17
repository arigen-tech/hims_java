package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasAdmissionStatus;
import com.hims.entity.User;
import com.hims.entity.repository.MasAdmissionStatusRepository;
import com.hims.request.MasAdmissionStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionStatusResponse;
import com.hims.service.MasAdmissionStatusService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasAdmissionStatusServiceImpl implements MasAdmissionStatusService {
    @Autowired
    private MasAdmissionStatusRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasAdmissionStatusResponse>> getAll(int flag) {
        try {
            List<MasAdmissionStatus> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByStatusCodeAsc("y")
                            : repository.findAllByOrderByLastUpdateDateDesc();

            List<MasAdmissionStatusResponse> res =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(res, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionStatusResponse> getById(Long id) {
        MasAdmissionStatus obj = repository.findById(id).orElse(null);

        if (obj == null)
            return ResponseUtils.createNotFoundResponse("Admission Status ID not found", 404);

        return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasAdmissionStatusResponse> create(MasAdmissionStatusRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasAdmissionStatus obj = MasAdmissionStatus.builder()
                    .statusCode(request.getStatusCode())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            MasAdmissionStatus saved = repository.save(obj);

            return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Create failed: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionStatusResponse> update(Long id, MasAdmissionStatusRequest request) {
        try {
            MasAdmissionStatus obj = repository.findById(id).orElse(null);

            if (obj == null)
                return ResponseUtils.createNotFoundResponse("Admission Status not found", 404);

            User user = authUtil.getCurrentUser();

            obj.setStatusCode(request.getStatusCode());
            obj.setLastUpdatedBy(user.getFirstName());
            obj.setLastUpdateDate(LocalDateTime.now());

            repository.save(obj);

            return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Update failed: " + e.getMessage(), 500
            );
        }
    }

    @Override
    public ApiResponse<MasAdmissionStatusResponse> changeStatus(Long id, String status) {
        try {
            MasAdmissionStatus obj = repository.findById(id).orElse(null);

            if (obj == null)
                return ResponseUtils.createNotFoundResponse("Admission Status not found", 404);

            if (!status.equals("y") && !status.equals("n"))
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status",
                        400
                );

            User user = authUtil.getCurrentUser();

            obj.setStatus(status);
            obj.setLastUpdatedBy(user.getFirstName());
            obj.setLastUpdateDate(LocalDateTime.now());

            repository.save(obj);

            return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Status update failed", 500
            );
        }
    }

    private MasAdmissionStatusResponse toResponse(MasAdmissionStatus m) {
        return new MasAdmissionStatusResponse(
                m.getAdmissionStatusId(),
                m.getStatusCode(),
                m.getStatus(),
                m.getLastUpdateDate(),
                m.getCreatedBy(),
                m.getLastUpdatedBy()
        );
    }

}
