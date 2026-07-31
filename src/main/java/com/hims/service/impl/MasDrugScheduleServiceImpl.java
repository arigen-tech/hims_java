package com.hims.service.impl;
// ========================= SERVICE IMPL =========================

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasDrugSchedule;
import com.hims.entity.User;
import com.hims.entity.repository.MasDrugScheduleRepository;
import com.hims.request.MasDrugScheduleRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDrugScheduleResponse;
import com.hims.service.MasDrugScheduleService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import kong.unirest.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasDrugScheduleServiceImpl implements MasDrugScheduleService {
     @Autowired
    private MasDrugScheduleRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasDrugScheduleResponse>> getAllSchedule(int flag) {

        try {

            List<MasDrugSchedule> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByScheduleCodeAsc(
                    AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error fetching schedule list", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<MasDrugScheduleResponse> getScheduleById(String id) {

        try {

            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            mapToResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Schedule not found",
                            HttpStatus.NOT_FOUND
                    ));

        } catch (Exception e) {

            log.error("Error fetching schedule by id", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<MasDrugScheduleResponse> createSchedule(
            MasDrugScheduleRequest request) {

        try {

            User user = authUtil.getCurrentUser();

            MasDrugSchedule entity = new MasDrugSchedule();

            entity.setScheduleCode(request.getScheduleCode());
            entity.setScheduleName(request.getScheduleName());
            entity.setLegalDescription(request.getLegalDescription());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error creating schedule", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<MasDrugScheduleResponse> updateSchedule(
            String id,
            MasDrugScheduleRequest request) {

        try {

            MasDrugSchedule entity = repository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse(
                        "Schedule not found",
                        HttpStatus.NOT_FOUND
                );
            }

            User user = authUtil.getCurrentUser();
            entity.setScheduleName(request.getScheduleName());
            entity.setLegalDescription(request.getLegalDescription());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error updating schedule", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<MasDrugScheduleResponse> changeStatus(
            String id,
            String status) {

        try {

            MasDrugSchedule entity = repository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("Schedule not found", HttpStatus.NOT_FOUND
                );
            }

            User user = authUtil.getCurrentUser();

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error changing schedule status", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private MasDrugScheduleResponse mapToResponse(MasDrugSchedule entity) {

        MasDrugScheduleResponse response = new MasDrugScheduleResponse();

        response.setScheduleCode(entity.getScheduleCode());
        response.setScheduleName(entity.getScheduleName());
        response.setLegalDescription(entity.getLegalDescription());
        response.setStatus(entity.getStatus());
        response.setLastUpdateDate(entity.getLastUpdateDate());

        return response;
    }
}