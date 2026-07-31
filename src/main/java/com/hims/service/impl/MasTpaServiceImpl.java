package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasTpa;
import com.hims.entity.User;
import com.hims.entity.repository.MasTpaRepository;
import com.hims.request.MasTpaRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTpaResponse;
import com.hims.service.MasTpaService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MasTpaServiceImpl implements MasTpaService {
    @Autowired
    private MasTpaRepository repository;
    @Autowired
    private AuthUtil authUtil;
    @Override
    public ApiResponse<List<MasTpaResponse>> getAllMasTpa(int flag) {

        log.info("Fetching TPA list, flag={}", flag);

        try {
            List<MasTpa> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByTpaNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastChgDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching TPA list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    @Override
    public ApiResponse<MasTpaResponse> getByIdTpa(Long id) {

        log.info("Fetching TPA by id={}", id);

        try {

            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            mapToResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "TPA not found",
                            404
                    ));

        } catch (Exception e) {

            log.error("Error fetching TPA by id={}", id, e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasTpaResponse> createTpa(MasTpaRequest request) {

        log.info("Creating TPA");

        try {

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        404
                );
            }

            MasTpa entity = new MasTpa();

            entity.setTpaName(request.getTpaName());
            entity.setTpaCode(request.getTpaCode());
            entity.setContactPerson(request.getContactPerson());
            entity.setContactNo(request.getContactNo());
            entity.setEmailId(request.getEmailId());
            entity.setAddress(request.getAddress());

            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error creating TPA", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasTpaResponse> updateTpa(Long id, MasTpaRequest request) {

        log.info("Updating TPA id={}", id);

        try {

            MasTpa entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "TPA not found",
                        404
                );
            }

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        404
                );
            }

            entity.setTpaName(request.getTpaName());
            entity.setTpaCode(request.getTpaCode());
            entity.setContactPerson(request.getContactPerson());
            entity.setContactNo(request.getContactNo());
            entity.setEmailId(request.getEmailId());
            entity.setAddress(request.getAddress());

            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error updating TPA id={}", id, e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasTpaResponse> changeStatusTpa(Long id, String status) {

        log.info("Changing TPA status, id={}, status={}", id, status);

        try {

            MasTpa entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "TPA not found",
                        404
                );
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase())
                    && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {

                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status",
                        400
                );
            }

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        404
                );
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error changing TPA status id={}", id, e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    private MasTpaResponse mapToResponse(MasTpa entity) {

        MasTpaResponse res = new MasTpaResponse();

        res.setTpaId(entity.getTpaId());
        res.setTpaName(entity.getTpaName());
        res.setTpaCode(entity.getTpaCode());
        res.setContactPerson(entity.getContactPerson());
        res.setContactNo(entity.getContactNo());
        res.setEmail(entity.getEmailId());
        res.setAddress(entity.getAddress());
        res.setLastChgDate(entity.getLastChgDate());
        res.setStatus(entity.getStatus());

        return res;
    }
}
