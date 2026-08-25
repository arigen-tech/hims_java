package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasOperationTheatre;
import com.hims.entity.User;
import com.hims.entity.repository.MasOperationTheatreRepository;
import com.hims.request.OperationTheatreRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OperationTheatreResponse;
import com.hims.service.MasOperationTheatreService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import kong.unirest.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MasOperationTheatreServiceImpl implements MasOperationTheatreService {

    @Autowired
    private MasOperationTheatreRepository masOperationTheatreRepository;

    @Autowired
    private AuthUtil authUtil;


    @Override
    public ApiResponse<String> saveOperationTheatre(OperationTheatreRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();

            MasOperationTheatre entity = new MasOperationTheatre();

            entity.setOtCode(request.getOtCode());
            entity.setOtName(request.getOtName());
            entity.setOtType(request.getOtType());
            entity.setLocation(request.getLocation());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            masOperationTheatreRepository.save(entity);

            return ResponseUtils.createSuccessResponse("Operation Theatre created successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error creating Operation Theatre", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ApiResponse<List<OperationTheatreResponse>>
    getAllOperationTheatres(int flag) {

        log.info("Fetching Operation Theatre list, flag={}", flag);

        try {

            List<MasOperationTheatre> list;

            if (flag == 1) {
                list = masOperationTheatreRepository.findByStatusIgnoreCaseOrderByOtNameAsc(AppConstants.STATUS_Y.toLowerCase());

            } else {

                list = masOperationTheatreRepository.findAllByOrderByStatusDescLastChgDateDesc();
            }

            List<OperationTheatreResponse> response = list
                    .stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching Operation Theatre list", e);

            return ResponseUtils.createFailureResponse(null,new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @Override
    public ApiResponse<OperationTheatreResponse> getById(Long id) {

        try {

            MasOperationTheatre entity = masOperationTheatreRepository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Operation Theatre not found",
                        HttpStatus.NOT_FOUND);
            }

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error fetching Operation Theatre id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @Override
    @Transactional
    public ApiResponse<OperationTheatreResponse>
    changeStatus(Long id, String status) {

        try {

            MasOperationTheatre entity = masOperationTheatreRepository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse(
                        "Operation Theatre not found",
                        HttpStatus.NOT_FOUND
                );
            }
            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase())
                    && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase()))
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status value and value should bi y and n",
                        400
                );
            User currentUser = authUtil.getCurrentUser();
            entity.setStatus(status.toLowerCase());
            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            masOperationTheatreRepository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error changing status for Operation Theatre id: {}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @Override

    public ApiResponse<String> updateOperationTheatre(
            Long id,
            OperationTheatreRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();
            MasOperationTheatre entity = masOperationTheatreRepository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Operation Theatre not found",
                        HttpStatus.NOT_FOUND
                );
            }

            entity.setOtCode(request.getOtCode());
            entity.setOtName(request.getOtName());
            entity.setOtType(request.getOtType());
            entity.setLocation(request.getLocation());

            entity.setLastChgBy(currentUser.getFullName());
            entity.setLastChgDate(LocalDateTime.now());

            masOperationTheatreRepository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    "Operation Theatre updated successfully",
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error updating Operation Theatre id: {}",id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    private OperationTheatreResponse toResponse(
            MasOperationTheatre entity) {

        OperationTheatreResponse response =
                new OperationTheatreResponse();

        response.setOtId(entity.getOtId());
        response.setOtCode(entity.getOtCode());
        response.setOtName(entity.getOtName());
        response.setOtType(entity.getOtType());
        response.setLocation(entity.getLocation());
        response.setStatus(entity.getStatus());
        response.setLastChgBy(entity.getLastChgBy());
        response.setLastChgDate(entity.getLastChgDate());

        return response;
    }
}