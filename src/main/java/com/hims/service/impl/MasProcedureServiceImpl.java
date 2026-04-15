package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasProcedure;
import com.hims.entity.MasProcedureType;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasProcedureRepository;
import com.hims.entity.repository.MasProcedureTypeRepository;
import com.hims.projection.MasProcedureProjection;
import com.hims.request.MasProcedureRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedureResponse;
import com.hims.service.MasProcedureService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
            List<MasProcedureProjection> list = repository.findAllMasProcedure(flag,AppConstants.STATUS_Y.toLowerCase());

            List<MasProcedureResponse> response = list.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("MasProcedure: Error fetching list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<MasProcedureResponse>> getAllProceduresWIthFilter(
            int flag, int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MasProcedure> procedurePage;

        boolean hasSearch = (search != null && !search.trim().isEmpty());
        String searchPattern = "%" + search.toLowerCase() + "%";

        if (hasSearch) {

            //  If flag = 1 → Only status = 'Y'
            if (flag == 1) {
                procedurePage = repository.searchProcedure(
                        AppConstants.STATUS_Y.toLowerCase(), searchPattern, pageable
                );
            }
            //  Flag != 1 → status IN (Y, N)
            else {
                procedurePage = repository.searchProcedureIn(
                        List.of(AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase()), searchPattern, pageable
                );
            }
        }
        else if (flag == 1) {
            procedurePage = repository.findByStatusIgnoreCase(AppConstants.STATUS_Y.toLowerCase(), pageable);
        } else {
            procedurePage = repository.findByStatusInIgnoreCase(List.of(AppConstants.STATUS_Y, AppConstants.STATUS_N.toLowerCase()), pageable);
        }

        Page<MasProcedureResponse> responsePage = procedurePage.map(this::toResponse);

        return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});
    }



    @Override
    public ApiResponse<MasProcedureResponse> getMasProcedureById(Long id) {
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

        MasProcedure p = new MasProcedure();
        p.setProcedureCode(req.getProcedureCode());
        p.setProcedureName(req.getProcedureName());
        p.setProcedureLevel(req.getProcedureLevel().toUpperCase());
        p.setDepartment(departmentRepository.findById(req.getDepartmentId()).orElseThrow());
        p.setLastChgBy(user.getFullName());
        p.setIpdAllowed(req.getIpdAllowed().toUpperCase());
        p.setStatus(AppConstants.STATUS_Y.toLowerCase());
        p.setLastChgDate(LocalDateTime.now());
        p.setOpdAllowed(req.getOpdAllowed().toUpperCase());
        p.setIsNursing(req.getIsNursing().toUpperCase());
        MasProcedure saved = repository.save(p);

        log.info("MasProcedure Created | id={}", saved.getProcedureId());
        return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasProcedureResponse> updateMasProcedure(Long id, MasProcedureRequest req) {
        log.info("MasProcedure: Update Start | id={} | data={}", id, req);

        User user = authUtil.getCurrentUser();
        MasProcedure procedure = repository.findById(id).orElse(null);
        if (procedure == null) {
            log.warn("Update failed, id not found={}", id);
            return ResponseUtils.createNotFoundResponse("Procedure not found", 404);
        }
        procedure.setProcedureCode(req.getProcedureCode());
        procedure.setProcedureName(req.getProcedureName());
        procedure.setProcedureLevel(req.getProcedureLevel().toUpperCase());
        procedure.setDepartment(departmentRepository.findById(req.getDepartmentId()).orElseThrow());
        procedure.setLastChgBy(user.getFullName());
        procedure.setIpdAllowed(req.getIpdAllowed().toUpperCase());
        procedure.setStatus(AppConstants.STATUS_Y.toLowerCase());
        procedure.setLastChgDate(LocalDateTime.now());
        procedure.setOpdAllowed(req.getOpdAllowed().toUpperCase());
        procedure.setIsNursing(req.getIsNursing().toUpperCase());
        MasProcedure saved = repository.save(procedure);
        log.info("MasProcedure Updated | id={}", id);
        return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});
    }
    @Override
    public ApiResponse<MasProcedureResponse>changeStatus(Long id, String status) {
        log.info("MasProcedure: Change Status | id={} | status={}", id, status);

        User user = authUtil.getCurrentUser();
        MasProcedure procedure = repository.findById(id).orElse(null);
        if (procedure == null) {
            log.warn("Status change failed: id not found");
            return ResponseUtils.createNotFoundResponse("Procedure not found", 404);
        }
        if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
            log.warn("Invalid status value={}", status);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status must be y or n", HttpStatus.BAD_REQUEST.value());
        }

        procedure.setStatus(status);
        procedure.setLastChgDate(LocalDateTime.now());
        procedure.setLastChgBy(user.getFullName());
        MasProcedure saved = repository.save(procedure);

        log.info("MasProcedure: Status changed | id={} | newStatus={}", id, status);
        return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});
    }
    private MasProcedureResponse toResponse(MasProcedure p) {

        MasProcedureResponse res = new MasProcedureResponse();

        res.setProcedureId(p.getProcedureId());
        res.setProcedureCode(p.getProcedureCode());
        res.setProcedureName(p.getProcedureName());
        res.setStatus(p.getStatus());
        res.setLastChgBy(p.getLastChgBy());
        res.setLastChgDate(p.getLastChgDate());
        if (p.getDepartment() != null) {
            res.setDepartmentId(p.getDepartment().getId());
            res.setDepartmentName(p.getDepartment().getDepartmentName());
        }
        res.setOpdAllowed(p.getOpdAllowed());
        res.setIpdAllowed(p.getIpdAllowed());
        res.setIsNursing(p.getIsNursing());
        res.setProcedureLevel(p.getProcedureLevel());
        return res;
    }
    private MasProcedureResponse mapToResponse(MasProcedureProjection p) {

        MasProcedureResponse res = new MasProcedureResponse();

        res.setProcedureId(p.getProcedureId());
        res.setProcedureCode(p.getProcedureCode());
        res.setProcedureName(p.getProcedureName());
        res.setStatus(p.getStatus());
        res.setLastChgBy(p.getLastChgBy());
        res.setLastChgDate(p.getLastChgDate());
        res.setDepartmentId(p.getDepartmentId());
        res.setDepartmentName(p.getDepartmentName());
        res.setOpdAllowed(p.getOpdAllowed());
        res.setIpdAllowed(p.getIpdAllowed());
        res.setIsNursing(p.getIsNursing());
        res.setProcedureLevel(p.getProcedureLevel());

        return res;
    }
}
