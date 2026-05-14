package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasInsurance;
import com.hims.entity.User;
import com.hims.entity.repository.MasInsuranceRepository;
import com.hims.request.MasInsuranceRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInsuranceResponse;
import com.hims.service.MasInsuranceService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AUTH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MasInsuranceServiceImpl implements MasInsuranceService {
    @Autowired
    private MasInsuranceRepository repository;
    @Autowired
    private AuthUtil authUtil;
    @Override
    public ApiResponse<List<MasInsuranceResponse>> getAllMasInsurance(int flag) {

        log.info("Fetching Insurance list, flag={}", flag);

        try {
            List<MasInsurance> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByInsuranceNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastChgDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching Insurance list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<MasInsuranceResponse> getByIdInsurance(Long id) {
        log.info("Fetching insurance by id={}", id);

        try {
            return repository.findById(id).map(e -> ResponseUtils.createSuccessResponse(
                            mapToResponse(e),
                            new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse("Insurance not found",
                            404));

        } catch (Exception e) {
            log.error("Error fetching insurance by id={}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500);
        }
    }

    @Override
    public ApiResponse<MasInsuranceResponse> createInsurance(MasInsuranceRequest request) {

        log.info("Creating insurance");

        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Current user not found", 404);
            }
            MasInsurance entity = new MasInsurance();

            entity.setInsuranceName(request.getInsuranceName());
            entity.setInsuranceCode(request.getInsuranceCode());
            entity.setContactPerson(request.getContactPerson());
            entity.setContactNo(request.getContactNo());
            entity.setEmailId(request.getEmailId());
            entity.setAddress(request.getAddress());

            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error creating insurance", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500);
        }
    }

    @Override
    public ApiResponse<MasInsuranceResponse> updateInsurance(Long id, MasInsuranceRequest request) {

        log.info("Updating insurance id={}", id);

        try {

            MasInsurance entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Insurance not found", 404);
            }
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found",
                        404);
            }

            entity.setInsuranceName(request.getInsuranceName());
            entity.setInsuranceCode(request.getInsuranceCode());
            entity.setContactPerson(request.getContactPerson());
            entity.setContactNo(request.getContactNo());
            entity.setEmailId(request.getEmailId());
            entity.setAddress(request.getAddress());

            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error updating insurance id={}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500);
        }
    }

    @Override
    public ApiResponse<MasInsuranceResponse> changeStatusInsurance(Long id, String status) {

        log.info("Changing insurance status, id={}, status={}", id, status);

        try {
            MasInsurance entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Insurance not found", 404);
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase()) && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status", 400);
            }

            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found",
                        404);
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error changing insurance status id={}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500);
        }
    }
    private MasInsuranceResponse mapToResponse(MasInsurance entity) {

        MasInsuranceResponse res = new MasInsuranceResponse();

        res.setInsuranceId(entity.getInsuranceId());
        res.setInsuranceName(entity.getInsuranceName());
        res.setInsuranceCode(entity.getInsuranceCode());
        res.setContactPerson(entity.getContactPerson());
        res.setContactNo(entity.getContactNo());

        res.setLastChgDate(entity.getLastChgDate());
        res.setStatus(entity.getStatus());
        res.setEmail(entity.getEmailId());
        res.setAddress(entity.getAddress());

        return res;
    }
}
