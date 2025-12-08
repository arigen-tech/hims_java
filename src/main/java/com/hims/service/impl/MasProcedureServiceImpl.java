package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasProcedure;
import com.hims.entity.MasProcedureType;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasProcedureRepository;
import com.hims.entity.repository.MasProcedureTypeRepository;
import com.hims.request.MasProcedureRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedureResponse;
import com.hims.service.MasProcedureService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Builder
public class MasProcedureServiceImpl implements MasProcedureService {

    @Autowired
    private MasProcedureRepository repository;

    @Autowired
    private MasDepartmentRepository departmentRepository;

    @Autowired
    private MasProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasProcedureResponse>> getAllMasProcedure(int flag) {
        log.info("MasProcedure: Fetch All | flag={}", flag);

        try {
            List<MasProcedure> list;

            if (flag == 1) {
                log.info("Fetching active procedures only");
                list = repository.findByStatusIgnoreCase("y");
            } else if (flag == 0) {
                log.info("Fetching all procedures");
                list = repository.findAll();
            } else {
                log.warn("Invalid flag: {}", flag);
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value. Use 0 or 1.", 400);
            }

            List<MasProcedureResponse> response = list.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            log.info("MasProcedure: Fetch All Success, Count={}", response.size());
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("MasProcedure: Error fetching list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Unexpected error: " + e.getMessage(), 500);
        }
    }
    @Override
    public ApiResponse<MasProcedureResponse> getMasProcedureById(Integer id) {
        log.info("MasProcedure: Find By ID | id={}", id);

        Optional<MasProcedure> procedure = repository.findById(id);

        if (procedure.isEmpty()) {
            log.warn("MasProcedure: Not found | id={}", id);
            return ResponseUtils.createNotFoundResponse("Procedure not found", 404);
        }

        return ResponseUtils.createSuccessResponse(toResponse(procedure.get()), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasProcedureResponse> addMasProcedure(MasProcedureRequest req) {
        log.info("MasProcedure: Create request={}", req);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        User user = authUtil.getCurrentUser();
        Long depart= authUtil.getCurrentDepartmentId();
        if (user == null) {
            log.error("Create failed: current user not found");
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", 400);
        }

        MasDepartment department = departmentRepository.findById(depart)
                .orElse(null);

        MasProcedureType procedureType = procedureTypeRepository.findById(req.getProcedureTypeId())
                .orElse(null);

        MasProcedure p = MasProcedure.builder()

                .procedureCode(req.getProcedureCode())
                .procedureName(req.getProcedureName())
                .defaultStatus("y")
                .status("y")
                .procedureGroup(req.getProcedureGroup())
                .department(department)
                .procedureType(procedureType)
                .lastChangedBy(user.getFirstName())
                .lastChangedTime(LocalTime.now().format(timeFormatter))
                .lastChangedDate(LocalDateTime.now())
                .build();

        MasProcedure saved = repository.save(p);

        log.info("MasProcedure Created | id={}", saved.getProcedureId());
        return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasProcedureResponse> updateMasProcedure(Integer id, MasProcedureRequest req) {
        log.info("MasProcedure: Update Start | id={} | data={}", id, req);

        User user = authUtil.getCurrentUser();
        Long depart=authUtil.getCurrentDepartmentId();
        if (user == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "User not found", 401);
        }

        MasProcedure procedure = repository.findById(id)
                .orElse(null);

        if (procedure == null) {
            log.warn("Update failed, id not found={}", id);
            return ResponseUtils.createNotFoundResponse("Procedure not found", 404);
        }

        MasDepartment department = departmentRepository.findById(depart).orElse(null);
        MasProcedureType type = procedureTypeRepository.findById(req.getProcedureTypeId()).orElse(null);

        procedure.setProcedureCode(req.getProcedureCode());
        procedure.setProcedureName(req.getProcedureName());
        procedure.setDefaultStatus("y");
        procedure.setProcedureGroup(req.getProcedureGroup());
        procedure.setDepartment(department);
        procedure.setProcedureType(type);
        procedure.setLastChangedBy(user.getFirstName());
        procedure.setLastChangedDate(LocalDateTime.now());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        procedure.setLastChangedTime(LocalTime.now().format(timeFormatter));
        procedure.setStatus("y");

        MasProcedure saved = repository.save(procedure);

        log.info("MasProcedure Updated | id={}", id);
        return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});
    }
    @Override
    public ApiResponse<MasProcedureResponse>changeStatus(Integer id, String status) {
        log.info("MasProcedure: Change Status | id={} | status={}", id, status);

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "User not found", 401);
        }

        MasProcedure procedure = repository.findById(id).orElse(null);

        if (procedure == null) {
            log.warn("Status change failed: id not found");
            return ResponseUtils.createNotFoundResponse("Procedure not found", 404);
        }

        if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
            log.warn("Invalid status value={}", status);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status must be y or n", 400);
        }

        procedure.setStatus(status.toUpperCase());
        procedure.setLastChangedDate(LocalDateTime.now());
        procedure.setLastChangedBy(user.getFirstName());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        procedure.setLastChangedTime(LocalTime.now().format(timeFormatter));
        MasProcedure saved = repository.save(procedure);

        log.info("MasProcedure: Status changed | id={} | newStatus={}", id, status);
        return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});
    }

    private MasProcedureResponse toResponse(MasProcedure p) {
        MasProcedureResponse res = new MasProcedureResponse();
        res.setProcedureId(p.getProcedureId());
        res.setProcedureCode(p.getProcedureCode());
        res.setProcedureName(p.getProcedureName());
        res.setDefaultStatus(p.getDefaultStatus());
        res.setStatus(p.getStatus());
        res.setProcedureGroup(p.getProcedureGroup());
        res.setLastChangedBy(p.getLastChangedBy());
        res.setLastChangedDate(p.getLastChangedDate());
        if (p.getDepartment() != null) {
            res.setDepartmentName(p.getDepartment().getDepartmentName());
        }
        if (p.getProcedureType() != null) {
            res.setProcedureTypeName(p.getProcedureType().getProcedureTypeName());
            res.setProcedureTypeId(p.getProcedureType().getProcedureTypeId());
        }
        return res;
    }
}
