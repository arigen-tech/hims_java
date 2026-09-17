package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBedStatus;
import com.hims.entity.repository.MasBedStatusRepo;
import com.hims.request.MasBedStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBedStatusResponse;
import com.hims.response.UserContext;
import com.hims.service.MasBedStatusService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasBedStatusServiceImpl implements MasBedStatusService {

    private final MasBedStatusRepo masBedStatusRepo;
    private final AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<MasBedStatusResponse> createBedStatus(MasBedStatusRequest request) {
        try {
            log.info("createBedStatus() method Started...");

            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasBedStatus entity = new MasBedStatus();
            entity.setBedStatusName(request.getBedStatusName());
            entity.setStatus("y");
            entity.setLastUpdateDate(LocalDate.now());
            entity.setCreatedBy(userContext.getUserFullName());
            entity.setLastUpdatedBy(userContext.getUserFullName());

            MasBedStatus saved = masBedStatusRepo.save(entity);

            log.info("createBedStatus() method Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("createBedStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasBedStatusResponse> updateBedStatus(Long bedStatusId, MasBedStatusRequest request) {
        try {
            log.info("updateBedStatus() method Started...");

            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasBedStatus entity = masBedStatusRepo.findById(bedStatusId)
                    .orElseThrow(() -> new RuntimeException("Invalid Bed Status Id"));

            entity.setBedStatusName(request.getBedStatusName());
            entity.setLastUpdatedBy(userContext.getUserFullName());
            entity.setLastUpdateDate(LocalDate.now());

            MasBedStatus saved = masBedStatusRepo.save(entity);

            log.info("updateBedStatus() method Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("updateBedStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasBedStatusResponse> changeActiveStatus(Long bedStatusId, String status) {
        try {
            log.info("changeActiveStatus() method Started...");

            UserContext userContext = userContextService.getCurrentUserContext();
            if (userContext == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasBedStatus entity = masBedStatusRepo.findById(bedStatusId)
                    .orElseThrow(() -> new RuntimeException("Invalid Bed Status Id"));

            entity.setStatus(status);
            entity.setLastUpdatedBy(userContext.getUserFullName());
            entity.setLastUpdateDate(LocalDate.now());

            MasBedStatus saved = masBedStatusRepo.save(entity);

            log.info("changeActiveStatus() method Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("changeActiveStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasBedStatusResponse> getById(Long bedStatusId) {
        try {
            log.info("getById() method Started...");

            MasBedStatus entity = masBedStatusRepo.findById(bedStatusId)
                    .orElseThrow(() -> new RuntimeException("Invalid Bed Status Id"));

            log.info("getById() method Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getById() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasBedStatusResponse>> getAll(int flag) {
        try {
            log.info("getAll() method Started...");

            List<MasBedStatus> list;

            if (flag == 0) {
                list = masBedStatusRepo.findAllByOrderByStatusDescLastUpdateDateDesc();
            } else if (flag == 1) {
                list = masBedStatusRepo.findByStatusIgnoreCaseOrderByBedStatusNameAsc("y");
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid Flag Value , Provide flag as 0 or 1", HttpStatus.BAD_REQUEST.value());
            }

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getAll() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasBedStatusResponse mapToResponse(MasBedStatus entity) {
        MasBedStatusResponse resp = new MasBedStatusResponse();
        resp.setBedStatusId(entity.getBedStatusId());
        resp.setBedStatusName(entity.getBedStatusName());
        resp.setStatus(entity.getStatus());
        resp.setLastUpdateDate(entity.getLastUpdateDate());
        return resp;
    }
}
