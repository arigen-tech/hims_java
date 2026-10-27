package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasProcedure;
import com.hims.entity.MasProcedureType;
import com.hims.entity.repository.MasProcedureRepository;
import com.hims.entity.repository.MasProcedureTypeRepository;
import com.hims.projection.MasProcedureProjection;
import com.hims.request.MasProcedureRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedureResponse;
import com.hims.response.UserContext;
import com.hims.service.MasProcedureService;
import com.hims.service.UserContextService;
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
    private MasProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

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
            int flag,
            int page,
            int size,
            String nursingStatus,
            String opdStatus,
            String ipdStatus,
            String search) {

        try {

            Pageable pageable = PageRequest.of(page, size);

            boolean hasSearch = search != null && !search.trim().isEmpty();

            String searchPattern = hasSearch ? "%" + search.trim().toLowerCase() + "%" : "%";

            nursingStatus = normalizeStatus(nursingStatus);
            opdStatus = normalizeStatus(opdStatus);
            ipdStatus = normalizeStatus(ipdStatus);

            List<String> statuses = flag == 1
                    ? List.of(AppConstants.STATUS_Y.toLowerCase())
                    : List.of(AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase());
            Page<MasProcedureResponse> responsePage = repository.findProcedureViewsWithFilters(
                    statuses, nursingStatus, opdStatus, ipdStatus,
                    hasSearch ? searchPattern : null, pageable)
                    .map(this::toResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});
        } catch (Exception ex) {
            log.error(
                    "Error while fetching procedures. flag={}, page={}, size={}, " +
                            "nursingStatus={}, opdStatus={}, ipdStatus={}, search={}",
                    flag,
                    page,
                    size,
                    nursingStatus,
                    opdStatus,
                    ipdStatus,
                    search,
                    ex
            );
            return ResponseUtils.createFailureResponse(  null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }


    private String normalizeStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        return status.trim().toUpperCase();
    }


    @Override
    public ApiResponse<MasProcedureResponse> getMasProcedureById(Long id) {
        log.info("MasProcedure: Find By ID | id={}", id);
        Optional<MasProcedureProjection> procedure = repository.findProcedureViewById(id);
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

        UserContext userContext = userContextService.getCurrentUserContext();

        MasProcedure p = new MasProcedure();
        Optional<MasProcedureType> procedureType = findProcedureType(req.getProcedureTypeId());
        if (procedureType.isEmpty()) {
            return invalidProcedureTypeResponse(req.getProcedureTypeId());
        }
        p.setProcedureCode(req.getProcedureCode());
        p.setProcedureName(req.getProcedureName());
        p.setProcedureType(procedureType.get());

        p.setLastChgBy(userContext.getUserFullName());
        p.setIpdAllowed(req.getIpdAllowed().toUpperCase());
        p.setStatus(AppConstants.STATUS_Y);
        p.setLastChgDate(LocalDateTime.now());
        p.setOpdAllowed(req.getOpdAllowed().toUpperCase());
        p.setIsNursing(req.getIsNursing().toUpperCase());
        MasProcedure saved = repository.save(p);

        log.info("MasProcedure Created | id={}", saved.getProcedureId());
        return ResponseUtils.createSuccessResponse(toResponse(repository.findProcedureViewById(saved.getProcedureId()).orElseThrow()), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasProcedureResponse> updateMasProcedure(Long id, MasProcedureRequest req) {
        log.info("MasProcedure: Update Start | id={} | data={}", id, req);

        UserContext userContext = userContextService.getCurrentUserContext();
        MasProcedure procedure = repository.findById(id).orElse(null);
        if (procedure == null) {
            log.warn("Update failed, id not found={}", id);
            return ResponseUtils.createNotFoundResponse("Procedure not found", 404);
        }
        Optional<MasProcedureType> procedureType = findProcedureType(req.getProcedureTypeId());
        if (procedureType.isEmpty()) {
            return invalidProcedureTypeResponse(req.getProcedureTypeId());
        }
        procedure.setProcedureCode(req.getProcedureCode());
        procedure.setProcedureName(req.getProcedureName());
        procedure.setProcedureType(procedureType.get());

        procedure.setLastChgBy(userContext.getUserFullName());
        procedure.setIpdAllowed(req.getIpdAllowed().toUpperCase());
        procedure.setStatus(AppConstants.STATUS_Y);
        procedure.setLastChgDate(LocalDateTime.now());
        procedure.setOpdAllowed(req.getOpdAllowed().toUpperCase());
        procedure.setIsNursing(req.getIsNursing().toUpperCase());
        MasProcedure saved = repository.save(procedure);
        log.info("MasProcedure Updated | id={}", id);
        return ResponseUtils.createSuccessResponse(toResponse(repository.findProcedureViewById(saved.getProcedureId()).orElseThrow()), new TypeReference<>() {});
    }
    @Override
    public ApiResponse<MasProcedureResponse>changeStatus(Long id, String status) {
        log.info("MasProcedure: Change Status | id={} | status={}", id, status);

        UserContext userContext = userContextService.getCurrentUserContext();
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

        procedure.setStatus(status.toUpperCase());
        procedure.setLastChgDate(LocalDateTime.now());
        procedure.setLastChgBy(userContext.getUserFullName());
        MasProcedure saved = repository.save(procedure);

        log.info("MasProcedure: Status changed | id={} | newStatus={}", id, status);
        return ResponseUtils.createSuccessResponse(toResponse(repository.findProcedureViewById(saved.getProcedureId()).orElseThrow()), new TypeReference<>() {});
    }

    private Optional<MasProcedureType> findProcedureType(Long procedureTypeId) {
        return procedureTypeId == null ? Optional.empty() : procedureTypeRepository.findById(procedureTypeId);
    }

    private ApiResponse<MasProcedureResponse> invalidProcedureTypeResponse(Long procedureTypeId) {
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                procedureTypeId == null ? "Procedure type ID is required" : "Procedure type not found: " + procedureTypeId,
                HttpStatus.BAD_REQUEST.value());
    }

    private MasProcedureResponse toResponse(MasProcedureProjection p) {

        MasProcedureResponse res = new MasProcedureResponse();

        res.setProcedureId(p.getProcedureId());
        res.setProcedureCode(p.getProcedureCode());
        res.setProcedureName(p.getProcedureName());
        res.setStatus(p.getStatus());
        res.setLastChgBy(p.getLastChgBy());
        res.setLastChgDate(p.getLastChgDate());

        res.setOpdAllowed(p.getOpdAllowed());
        res.setIpdAllowed(p.getIpdAllowed());
        res.setIsNursing(p.getIsNursing());
        res.setProcedureTypeId(p.getProcedureTypeId());
        res.setProcedureTypeName(p.getProcedureTypeName());

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

        res.setOpdAllowed(p.getOpdAllowed());
        res.setIpdAllowed(p.getIpdAllowed());
        res.setIsNursing(p.getIsNursing());
        res.setProcedureTypeId(p.getProcedureTypeId());
        res.setProcedureTypeName(p.getProcedureTypeName());

        return res;
    }
}
