package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasAdmissionSource;
import com.hims.entity.User;
import com.hims.entity.repository.MasAdmissionSourceRepository;
import com.hims.request.MasAdmissionSourceRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionSourceResponse;
import com.hims.service.MasAdmissionSourceService;
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
public class MasAdmissionSourceServiceImpl implements MasAdmissionSourceService {

    @Autowired
    private MasAdmissionSourceRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasAdmissionSourceResponse>> getAllMasAdmissionSource(int flag) {

        log.info("Fetching Admission Source list, flag={}", flag);
        try {
            List<MasAdmissionSource> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByAdmissionSourceNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(list.stream().map(this::mapToResponse).toList(), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching Admission Source list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionSourceResponse> getByIdMasAdmissionSource(Long id) {

        log.info("Fetching Admission Source by id={}", id);

        try {
            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            mapToResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse("Admission Source not found", 404));

        } catch (Exception e) {

            log.error("Error fetching Admission Source by id={}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionSourceResponse> createMasAdmissionSource(MasAdmissionSourceRequest request) {

        log.info("Creating Admission Source");

        try {
            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found", 404);
            }

            MasAdmissionSource entity = new MasAdmissionSource();

            entity.setAdmissionSourceName(request.getAdmissionSourceName());
            entity.setDescription(request.getDescription());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error creating Admission Source", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionSourceResponse> updateMasAdmissionSource(Long id, MasAdmissionSourceRequest request) {

        log.info("Updating Admission Source id={}", id);

        try {
            MasAdmissionSource entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Admission Source not found", 404);
            }

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found", 404);
            }
            entity.setAdmissionSourceName(request.getAdmissionSourceName());
            entity.setDescription(request.getDescription());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error updating Admission Source id={}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<MasAdmissionSourceResponse> changeStatusMasAdmissionSource(
            Long id,
            String status) {

        log.info("Changing Admission Source status, id={}, status={}", id, status);

        try {
            MasAdmissionSource entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Admission Source not found", 404);
            }
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(null,    "Current user not found", 404);
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error changing Admission Source status id={}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    private MasAdmissionSourceResponse mapToResponse(MasAdmissionSource entity) {
        MasAdmissionSourceResponse res = new MasAdmissionSourceResponse();
        res.setId(entity.getId());
        res.setAdmissionSourceName(entity.getAdmissionSourceName());
        res.setDescription(entity.getDescription());
        res.setStatus(entity.getStatus());
        res.setCreatedBy(entity.getCreatedBy());
        res.setLastUpdatedBy(entity.getLastUpdatedBy());
        return res;
    }

}