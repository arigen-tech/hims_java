package com.hims.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasOtBookingStatus;
import com.hims.entity.User;
import com.hims.entity.repository.MasOtBookingStatusRepository;
import com.hims.request.MasOtBookingStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtBookingStatusResponse;
import com.hims.service.MasOtBookingStatusService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MasOtBookingStatusServiceImpl implements MasOtBookingStatusService {

    @Autowired
    private MasOtBookingStatusRepository masOtBookingStatusRepository;

    @Autowired
    private AuthUtil authUtil;

    // CREATE
    @Override
    public ApiResponse<String> saveOtBookingStatus(MasOtBookingStatusRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();

            Optional<MasOtBookingStatus> existing =
                    masOtBookingStatusRepository.findByStatusCodeIgnoreCase(request.getStatusCode());

            if (existing.isPresent()) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Status code already exists", HttpStatus.CONFLICT.value());
            }

            MasOtBookingStatus entity = new MasOtBookingStatus();

            entity.setStatusCode(request.getStatusCode());
            entity.setStatusName(request.getStatusName());
            entity.setDescription(request.getDescription());
            entity.setStatus(AppConstants.STATUS_Y.toUpperCase());
            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            masOtBookingStatusRepository.save(entity);

            return ResponseUtils.createSuccessResponse("OT Booking Status created successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error creating OT Booking Status", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // GET ALL
    @Override
    public ApiResponse<List<MasOtBookingStatusResponse>> getAllOtBookingStatus(int flag) {

        log.info("Fetching OT Booking Status list, flag={}", flag);

        try {

            List<MasOtBookingStatus> list;

            if (flag == 1) {

                list = masOtBookingStatusRepository.findByStatusIgnoreCaseOrderByStatusNameAsc(AppConstants.STATUS_Y.toLowerCase());

            } else {

                list = masOtBookingStatusRepository.findAllByOrderByStatusDescLastChgDateDesc();
            }

            List<MasOtBookingStatusResponse> response = list.stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching OT Booking Status list", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // GET BY ID
    @Override
    public ApiResponse<MasOtBookingStatusResponse> getById(Long id) {

        try {

            MasOtBookingStatus entity = masOtBookingStatusRepository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("OT Booking Status not found", HttpStatus.NOT_FOUND.value());
            }

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching OT Booking Status id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // CHANGE STATUS
    @Override
    public ApiResponse<MasOtBookingStatusResponse> changeStatus(Long id, String status) {

        try {

            MasOtBookingStatus entity = masOtBookingStatusRepository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("OT Booking Status not found", HttpStatus.NOT_FOUND.value());
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase())
                    && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status value and value should be y and n", 400);
            }

            User currentUser = authUtil.getCurrentUser();
            entity.setStatus(status.toUpperCase());
            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());
            masOtBookingStatusRepository.save(entity);

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error changing status for OT Booking Status id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // UPDATE
    @Override
    public ApiResponse<String> updateOtBookingStatus(Long id, MasOtBookingStatusRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();

            MasOtBookingStatus entity = masOtBookingStatusRepository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("OT Booking Status not found", HttpStatus.NOT_FOUND.value());
            }

            Optional<MasOtBookingStatus> existing =
                    masOtBookingStatusRepository.findByStatusCodeIgnoreCase(request.getStatusCode());

            if (existing.isPresent() && !existing.get().getBookingStatusId().equals(id)) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Status code already exists", HttpStatus.CONFLICT.value());
            }

            entity.setStatusCode(request.getStatusCode());
            entity.setStatusName(request.getStatusName());
            entity.setDescription(request.getDescription());
            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            masOtBookingStatusRepository.save(entity);

            return ResponseUtils.createSuccessResponse("OT Booking Status updated successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error updating OT Booking Status id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // ENTITY -> RESPONSE
    private MasOtBookingStatusResponse toResponse(MasOtBookingStatus entity) {

        MasOtBookingStatusResponse response = new MasOtBookingStatusResponse();

        response.setBookingStatusId(entity.getBookingStatusId());
        response.setStatusCode(entity.getStatusCode());
        response.setStatusName(entity.getStatusName());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setLastChgBy(entity.getLastChgBy());
        response.setLastChgDate(entity.getLastChgDate());

        return response;
    }
}