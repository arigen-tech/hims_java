package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasProcedureType;
import com.hims.entity.MasDepartment;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasProcedureTypeRepository;
import com.hims.request.MasProcedureTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedureTypeResponse;
import com.hims.response.UserContext;
import com.hims.service.MasProcedureTypeService;
import com.hims.service.UserContextService;
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
public class MasProcedureTypeServiceImpl implements MasProcedureTypeService {



    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private MasProcedureTypeRepository repository;

    @Autowired
    private MasDepartmentRepository departmentRepository;


    @Override
    public ApiResponse<List<MasProcedureTypeResponse>> getAllProcedureType(int flag) {
        log.info("MasProcedureType: Fetch All Start | flag={}", flag);

        try {
            if (flag != 0 && flag != 1) {
                log.warn("Invalid flag received: {}", flag);
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value. Use 0 or 1.", 400);
            }

            List<MasProcedureTypeResponse> responses = repository.findAllWithDepartment(flag == 1).stream()
                    .map(this::convertToResponse)
                    .toList();

            log.info("MasProcedureType: Fetch All Success | Records: {}", responses.size());
            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("MasProcedureType: Error while fetching list - {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasProcedureTypeResponse> findById(Long id) {
        log.info("MasProcedureType: Find By ID Start | id={}", id);

        Optional<MasProcedureTypeRepository.ProcedureTypeView> procedure = repository.findViewById(id);

        if (procedure.isEmpty()) {
            log.warn("MasProcedureType: ID not found | id={}", id);
            return ResponseUtils.createNotFoundResponse("Procedure type not found", 404);
        }

        log.info("MasProcedureType: Find By ID Success | id={}", id);
        return ResponseUtils.createSuccessResponse(convertToResponse(procedure.get()), new TypeReference<>() {});
    }


    @Override
    public ApiResponse<MasProcedureTypeResponse> addProcedureType(MasProcedureTypeRequest request) {
        log.info("MasProcedureType: Create Start | Data={}", request);

        Optional<MasDepartment> department = findDepartment(request.getDepartmentId());
        if (department.isEmpty()) {
            return invalidDepartmentResponse(request.getDepartmentId());
        }

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null) {
            log.error("Create failed: current user not found");
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", 400);
        }

        MasProcedureType type = MasProcedureType.builder()
                .procedureTypeName(request.getProcedureTypeName())
                .description(request.getDescription())
                .procedureTypeCode(request.getProcedureTypeCode())
                .department(department.get())
                .status("y")
                .createdBy(userContext.getUserFullName())
                .lastUpdatedBy(userContext.getUserFullName())
                .lastUpdateDate(LocalDateTime.now())
                .build();

        MasProcedureType saved = repository.save(type);

        log.info("MasProcedureType: Create Success | ID={}", saved.getProcedureTypeId());
        return ResponseUtils.createSuccessResponse(convertToResponse(repository.findViewById(saved.getProcedureTypeId()).orElseThrow()), new TypeReference<>() {});
    }


    @Override
    public ApiResponse<MasProcedureTypeResponse> update(Long id, MasProcedureTypeRequest request) {
        log.info("MasProcedureType: Update Start | id={} | Data={}", id, request);

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null) {
            log.error("Update failed: current user not found");
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", 401);
        }

        Optional<MasProcedureType> procedureOpt = repository.findById(id);
        if (procedureOpt.isEmpty()) {
            log.warn("Update failed: ID not found | id={}", id);
            return ResponseUtils.createNotFoundResponse("Procedure type not found", 404);
        }

        MasProcedureType procedure = procedureOpt.get();
        Optional<MasDepartment> department = findDepartment(request.getDepartmentId());
        if (department.isEmpty()) {
            return invalidDepartmentResponse(request.getDepartmentId());
        }
        procedure.setProcedureTypeName(request.getProcedureTypeName());
        procedure.setDescription(request.getDescription());
        procedure.setProcedureTypeCode(request.getProcedureTypeCode());
        procedure.setDepartment(department.get());
        procedure.setStatus("y");
        procedure.setLastUpdatedBy(userContext.getUserFullName());
        procedure.setLastUpdateDate(LocalDateTime.now());

        MasProcedureType saved = repository.save(procedure);

        log.info("MasProcedureType: Update Success | id={}", saved.getProcedureTypeId());
        return ResponseUtils.createSuccessResponse(convertToResponse(repository.findViewById(saved.getProcedureTypeId()).orElseThrow()), new TypeReference<>() {});
    }


    @Override
    public ApiResponse<MasProcedureTypeResponse> changeStatus(Long id, String status) {
        log.info("MasProcedureType: Change Status Start | id={} | status={}", id, status);

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null) {
            log.error("Change status failed: current user not found");
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", 401);
        }

        Optional<MasProcedureType> procedureOpt = repository.findById(id);
        if (procedureOpt.isEmpty()) {
            log.warn("Change status failed: ID not found | id={}", id);
            return ResponseUtils.createNotFoundResponse("Procedure type not found", 404);
        }

        if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
            log.warn("Invalid status value received: {}", status);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status must be y or n only", 400);
        }

        MasProcedureType procedure = procedureOpt.get();
        procedure.setStatus(status);
        procedure.setLastUpdatedBy(userContext.getUserFullName());
        procedure.setLastUpdateDate(LocalDateTime.now());

        MasProcedureType saved = repository.save(procedure);

        log.info("MasProcedureType: Change Status Success | id={} | newStatus={}",
                saved.getProcedureTypeId(), saved.getStatus());

        return ResponseUtils.createSuccessResponse(convertToResponse(repository.findViewById(saved.getProcedureTypeId()).orElseThrow()), new TypeReference<>() {});
    }


    private MasProcedureTypeResponse convertToResponse(MasProcedureTypeRepository.ProcedureTypeView type) {
        return new MasProcedureTypeResponse(
                type.getProcedureTypeId(),
                type.getProcedureTypeName(),
                type.getDescription(),
                type.getStatus(),
                type.getLastUpdateDate(),
                type.getCreatedBy(),
                type.getLastUpdatedBy(),
                type.getProcedureTypeCode(),
                type.getDepartmentId(),
                type.getDepartmentName()
        );
    }

    private Optional<MasDepartment> findDepartment(Long departmentId) {
        if (departmentId == null) {
            return Optional.empty();
        }
        return departmentRepository.findById(departmentId);
    }

    private ApiResponse<MasProcedureTypeResponse> invalidDepartmentResponse(Long departmentId) {
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                departmentId == null ? "Department ID is required" : "Department not found: " + departmentId,
                HttpStatus.BAD_REQUEST.value());
    }

}
