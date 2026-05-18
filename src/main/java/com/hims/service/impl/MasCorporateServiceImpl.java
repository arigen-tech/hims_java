package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasCorporate;
import com.hims.entity.User;
import com.hims.entity.repository.MasCorporateRepository;
import com.hims.request.MasCorporateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCorporateResponse;
import com.hims.service.MasCorporateService;
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
public class MasCorporateServiceImpl implements MasCorporateService {
    @Autowired
    private MasCorporateRepository repository;
    @Autowired
    private AuthUtil authUtil;
    @Override
    public ApiResponse<List<MasCorporateResponse>> getAllMasCorporate(int flag) {

        log.info("Fetching Corporate list, flag={}", flag);

        try {
            List<MasCorporate> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByCorporateNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastChgDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching Corporate list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    @Override
    public ApiResponse<MasCorporateResponse> getByIdCorporate(Long id) {

        log.info("Fetching Corporate by id={}", id);

        try {

            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            mapToResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Corporate not found",
                            404
                    ));

        } catch (Exception e) {

            log.error("Error fetching Corporate by id={}", id, e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasCorporateResponse> createCorporate(MasCorporateRequest request) {

        log.info("Creating Corporate");

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

            MasCorporate entity = new MasCorporate();

            entity.setCorporateName(request.getCorporateName());
            entity.setCorporateCode(request.getCorporateCode());
            entity.setContactPerson(request.getContactPerson());
            entity.setContactNo(request.getContactNo());
            entity.setEmailId(request.getEmailId());
            entity.setAddress(request.getAddress());
            entity.setCreditAllowed(request.getCreditAllowed());
            entity.setCreditDays(request.getCreditDays());

            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error creating Corporate", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasCorporateResponse> updateCorporate(Long id,
                                                             MasCorporateRequest request) {

        log.info("Updating Corporate id={}", id);

        try {

            MasCorporate entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Corporate not found",
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

            entity.setCorporateName(request.getCorporateName());
            entity.setCorporateCode(request.getCorporateCode());
            entity.setContactPerson(request.getContactPerson());
            entity.setContactNo(request.getContactNo());
            entity.setEmailId(request.getEmailId());
            entity.setAddress(request.getAddress());
            entity.setCreditAllowed(request.getCreditAllowed());
            entity.setCreditDays(request.getCreditDays());

            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error updating Corporate id={}", id, e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasCorporateResponse> changeStatusCorporate(Long id,
                                                                   String status) {

        log.info("Changing Corporate status, id={}, status={}", id, status);

        try {

            MasCorporate entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Corporate not found",
                        404
                );
            }

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found",
                        404
                );
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error changing Corporate status id={}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    private MasCorporateResponse mapToResponse(MasCorporate entity) {

        MasCorporateResponse res = new MasCorporateResponse();

        res.setCorporateId(entity.getCorporateId());
        res.setCorporateName(entity.getCorporateName());
        res.setCorporateCode(entity.getCorporateCode());
        res.setContactPerson(entity.getContactPerson());
        res.setContactNo(entity.getContactNo());

        res.setCreditAllowed(entity.getCreditAllowed());
        res.setCreditDays(entity.getCreditDays());

        res.setLastChgDate(entity.getLastChgDate());
        res.setStatus(entity.getStatus());
        res.setEmail(entity.getEmailId());
        res.setAddress(entity.getAddress());

        return res;
    }
}
