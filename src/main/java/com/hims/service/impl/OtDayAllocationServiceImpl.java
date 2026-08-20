package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;

import com.hims.constants.AppConstants;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOperationTheatre;
import com.hims.entity.OtDayAllocation;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasOperationTheatreRepository;
import com.hims.entity.repository.OtDayAllocationRepository;
import com.hims.request.OtDayAllocationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OtDayAllocationResponse;
import com.hims.service.OtDayAllocationService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import kong.unirest.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public  class OtDayAllocationServiceImpl implements OtDayAllocationService {

    @Autowired
    private OtDayAllocationRepository otDayAllocationRepository;

    @Autowired
    private MasOperationTheatreRepository masOperationTheatreRepository;

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    @Autowired
    private AuthUtil authUtil;


    // CREATE
    @Override
    public ApiResponse<String> saveOtDayAllocation(OtDayAllocationRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();

            MasOperationTheatre operationTheatre = masOperationTheatreRepository.findById(request.getOtId()).orElse(null);

            if (operationTheatre == null) {

                return ResponseUtils.createNotFoundResponse("Operation Theatre not found", HttpStatus.NOT_FOUND);
            }

            MasDepartment department = masDepartmentRepository.findById(request.getDepartmentId()).orElse(null);

            if (department == null) {
                return ResponseUtils.createNotFoundResponse("Department not found", HttpStatus.NOT_FOUND);
            }

            OtDayAllocation entity = new OtDayAllocation();

            entity.setOperationTheatre(operationTheatre);
            entity.setDepartment(department);
            entity.setDayOfWeek(request.getDayOfWeek());
            entity.setStartTime(request.getStartTime());
            entity.setEndTime(request.getEndTime());
            entity.setStatus(AppConstants.STATUS_Y.toUpperCase());
            entity.setLastChgBy(currentUser.getUserId());
            entity.setLastChgDate(LocalDateTime.now());

            otDayAllocationRepository.save(entity);

            return ResponseUtils.createSuccessResponse("OT Day Allocation created successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error creating OT Day Allocation", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<List<OtDayAllocationResponse>> getAllOtDayAllocations(int flag) {

        log.info("Fetching OT Day Allocation list, flag={}", flag);

        try {

            List<OtDayAllocation> list;

            if (flag == 1) {

                list = otDayAllocationRepository.findByStatusIgnoreCaseOrderByDayOfWeekAsc(AppConstants.STATUS_Y.toLowerCase());

            } else {

                list = otDayAllocationRepository.findAllByOrderByStatusDescLastChgDateDesc();
            }

            List<OtDayAllocationResponse> response = list.stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching OT Day Allocation list", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }



    @Override
    public ApiResponse<OtDayAllocationResponse> getById(
            Long id) {

        try {

            OtDayAllocation entity = otDayAllocationRepository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("OT Day Allocation not found", HttpStatus.NOT_FOUND );
            }

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching OT Day Allocation id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    // CHANGE STATUS
    @Override

    public ApiResponse<OtDayAllocationResponse> changeStatus(Long id, String status) {

        try {

            OtDayAllocation entity = otDayAllocationRepository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("OT Day Allocation not found", HttpStatus.NOT_FOUND
                );
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase()) && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status value and value should be y and n",
                        400
                );
            }

            User currentUser = authUtil.getCurrentUser();
            entity.setStatus(status.toUpperCase());
            entity.setLastChgBy(currentUser.getUserId());
            entity.setLastChgDate(LocalDateTime.now());
            otDayAllocationRepository.save(entity);

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error changing status for OT Day Allocation id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // UPDATE
    @Override
    public ApiResponse<String> updateOtDayAllocation(Long id, OtDayAllocationRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();

            OtDayAllocation entity = otDayAllocationRepository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("OT Day Allocation not found",HttpStatus.NOT_FOUND
                );
            }

            MasOperationTheatre operationTheatre = masOperationTheatreRepository.findById(request.getOtId()).orElse(null);

            if (operationTheatre == null) {

                return ResponseUtils.createNotFoundResponse("Operation Theatre not found", HttpStatus.NOT_FOUND);
            }

            MasDepartment department = masDepartmentRepository.findById(request.getDepartmentId()).orElse(null);

            if (department == null) {

                return ResponseUtils.createNotFoundResponse("Department not found", HttpStatus.NOT_FOUND);
            }

            entity.setOperationTheatre(operationTheatre);
            entity.setDepartment(department);
            entity.setDayOfWeek(request.getDayOfWeek());
            entity.setStartTime(request.getStartTime());
            entity.setEndTime(request.getEndTime());
            entity.setLastChgBy(currentUser.getUserId());
            entity.setLastChgDate(LocalDateTime.now());

            otDayAllocationRepository.save(entity);

            return ResponseUtils.createSuccessResponse("OT Day Allocation updated successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error updating OT Day Allocation id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    // ENTITY -> RESPONSE
    private OtDayAllocationResponse toResponse(OtDayAllocation entity) {

        OtDayAllocationResponse response = new OtDayAllocationResponse();

        response.setOtDayAllocationId(entity.getOtDayAllocationId());

        if (entity.getOperationTheatre() != null) {
            response.setOtId(entity.getOperationTheatre().getOtId());
            response.setOtCode(entity.getOperationTheatre().getOtCode());
            response.setOtName(entity.getOperationTheatre().getOtName()
            );
        }

        if (entity.getDepartment() != null) {
            response.setDepartmentId(entity.getDepartment().getId());
            response.setDepartmentName(entity.getDepartment().getDepartmentName());
        }

        response.setDayOfWeek(entity.getDayOfWeek());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setStatus(entity.getStatus());
        response.setLastChgBy(entity.getLastChgBy());
        response.setLastChgDate(entity.getLastChgDate());
        return response;
    }
}