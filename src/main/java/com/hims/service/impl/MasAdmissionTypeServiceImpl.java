package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasAdmissionType;
import com.hims.entity.User;
import com.hims.entity.repository.MasAdmissionTypeRepository;
import com.hims.request.MasAdmissionTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionTypeResponse;
import com.hims.service.MasAdmissionTypeService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasAdmissionTypeServiceImpl implements MasAdmissionTypeService {
    @Autowired
    private MasAdmissionTypeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasAdmissionTypeResponse>> getAll(int flag) {
        try {
            List<MasAdmissionType> list =
                    (flag == 1) ? repository.findByStatusIgnoreCaseOrderByAdmissionTypeNameAsc("y") : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            List<MasAdmissionTypeResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionTypeResponse> getById(Long id) {
        try {
            MasAdmissionType obj = repository.findById(id).orElse(null);

            if (obj == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

            return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionTypeResponse> create(MasAdmissionTypeRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasAdmissionType data = MasAdmissionType.builder()
                    .admissionTypeName(request.getAdmissionTypeName())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionTypeResponse> update(Long id, MasAdmissionTypeRequest request) {
        try {
            MasAdmissionType data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

            User user = authUtil.getCurrentUser();

            data.setAdmissionTypeName(request.getAdmissionTypeName());
            data.setDescription(request.getDescription());
            data.setLastUpdatedBy(user.getFirstName());
            data.setLastUpdateDate(LocalDateTime.now());

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionTypeResponse> changeStatus(Long id, String status) {
        try {
            MasAdmissionType data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

            if (!status.equals("y") && !status.equals("n"))
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid Status!", 400);

            User user = authUtil.getCurrentUser();

            data.setStatus(status);
            data.setLastUpdatedBy(user.getFirstName());
            data.setLastUpdateDate(LocalDateTime.now());

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    private MasAdmissionTypeResponse toResponse(MasAdmissionType m) {
        return new MasAdmissionTypeResponse(
                m.getAdmissionTypeId(),
                m.getAdmissionTypeName(),
                m.getDescription(),
                m.getStatus(),
                m.getLastUpdateDate(),
                m.getCreatedBy(),
                m.getLastUpdatedBy()
        );
    }

}
