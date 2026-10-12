package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasAdmissionType;
import com.hims.entity.repository.MasAdmissionTypeRepository;
import com.hims.request.MasAdmissionTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionTypeResponse;
import com.hims.response.UserContext;
import com.hims.service.MasAdmissionTypeService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.hims.constants.AppConstants.STATUS_N;
import static com.hims.constants.AppConstants.STATUS_Y;

@Service
public class MasAdmissionTypeServiceImpl implements MasAdmissionTypeService {
    @Autowired
    private MasAdmissionTypeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<MasAdmissionTypeResponse>> getAll(int flag) {
        try {
            List<MasAdmissionType> list =
                    (flag == 1) ? repository.findByStatusIgnoreCaseOrderByAdmissionTypeNameAsc(STATUS_Y.toLowerCase()) : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

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
            UserContext userContext = userContextService.getCurrentUserContext();

            MasAdmissionType data = MasAdmissionType.builder()
                    .admissionTypeName(request.getAdmissionTypeName())
                    .description(request.getDescription())
                    .status(STATUS_Y.toLowerCase())
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
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

            UserContext userContext = userContextService.getCurrentUserContext();

            data.setAdmissionTypeName(request.getAdmissionTypeName());
            data.setDescription(request.getDescription());
            data.setLastUpdatedBy(userContext.getUserFullName());
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

            if (!status.equals(STATUS_Y.toLowerCase()) && !status.equals(STATUS_N.toLowerCase()))
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid Status!", 400);

            UserContext userContext = userContextService.getCurrentUserContext();

            data.setStatus(status);
            data.setLastUpdatedBy(userContext.getUserFullName());
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
