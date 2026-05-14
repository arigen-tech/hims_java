package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.OpdHolidayMaster;
import com.hims.entity.User;
import com.hims.entity.repository.OpdHolidayMasterRepository;
import com.hims.request.OpdHolidayMasterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdHolidayMasterResponse;
import com.hims.service.OpdHolidayService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpdHolidayServiceImpl implements OpdHolidayService {

    private final OpdHolidayMasterRepository repository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<OpdHolidayMasterResponse>> getAllHoliday(int flag) {

        try {

            List<OpdHolidayMaster> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByHolidayNameAsc(
                    AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByLastUpdatedDtDesc();

            return ResponseUtils.createSuccessResponse(list.stream().map(this::mapToResponse).toList(), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching holiday list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<OpdHolidayMasterResponse> getHolidayById(Long id) {

        try {

            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            mapToResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse("Holiday not found", 404
                    ));

        } catch (Exception e) {

            log.error("Error fetching holiday by id", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }
    }

    @Override
    public ApiResponse<OpdHolidayMasterResponse> createHoliday(OpdHolidayMasterRequest request) {

        try {

            User user = authUtil.getCurrentUser();

            OpdHolidayMaster entity = new OpdHolidayMaster();

            entity.setHolidayDate(request.getHolidayDate());
            entity.setHolidayName(request.getHolidayName());
            entity.setRemarks(request.getRemarks());

            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());

            entity.setCreatedBy(user.getFullName());
            entity.setCreatedAt(LocalDateTime.now());

            entity.setUpdatedBy(user.getFullName());
            entity.setLastUpdatedDt(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error creating holiday", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<OpdHolidayMasterResponse> updateHoliday(
            Long id,
            OpdHolidayMasterRequest request) {

        try {

            OpdHolidayMaster entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Holiday not found",
                        404);
            }

            User user = authUtil.getCurrentUser();

            entity.setHolidayDate(request.getHolidayDate());
            entity.setHolidayName(request.getHolidayName());
            entity.setRemarks(request.getRemarks());

            entity.setUpdatedBy(user.getFullName());
            entity.setLastUpdatedDt(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error updating holiday", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<OpdHolidayMasterResponse> changeStatus(Long id, String status) {

        try {

            OpdHolidayMaster entity = repository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("Holiday not found", 404);
            }

            User user = authUtil.getCurrentUser();

            entity.setStatus(status.toLowerCase());
            entity.setUpdatedBy(user.getFullName());
            entity.setLastUpdatedDt(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error changing holiday status", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    private OpdHolidayMasterResponse mapToResponse(OpdHolidayMaster entity) {

        OpdHolidayMasterResponse response = new OpdHolidayMasterResponse();

        response.setOpdHolidayId(entity.getOpdHolidayId());
        response.setHolidayDate(entity.getHolidayDate());
        response.setHolidayName(entity.getHolidayName());
        response.setRemarks(entity.getRemarks());
        response.setStatus(entity.getStatus());

        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());

        response.setUpdatedBy(entity.getUpdatedBy());
        response.setLastUpdatedDt(entity.getLastUpdatedDt());

        return response;
    }
}