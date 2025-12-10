package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDietType;
import com.hims.entity.User;
import com.hims.entity.repository.MasDietTypeRepository;
import com.hims.request.MasDietTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDietTypeResponse;
import com.hims.service.MasDietTypeService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MasDietTypeServiceImpl implements MasDietTypeService {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private MasDietTypeRepository repository;

    @Override
    public ApiResponse<List<MasDietTypeResponse>> getAllDietType(int flag) {
        log.info("MasDietType: Fetch All Start | flag={}", flag);

        try {
            List<MasDietType> list;

            if (flag == 1) {
                list = repository.findByStatusIgnoreCase("y");
            } else if (flag == 0) {
                list = repository.findAll();
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value. Use 0 or 1.", 400);
            }

            List<MasDietTypeResponse> responses = list.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("MasDietType: Error while fetching list - {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Unexpected error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasDietTypeResponse> findById(Long id) {
        Optional<MasDietType> diet = repository.findById(id);

        if (diet.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Diet type not found", 404);
        }

        return ResponseUtils.createSuccessResponse(convertToResponse(diet.get()),
                new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasDietTypeResponse> addDietType(MasDietTypeRequest request) {

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", 400);
        }

        MasDietType diet = MasDietType.builder()
                .dietTypeName(request.getDietTypeName())
                .description(request.getDescription())
                .status("y")
                .createdBy(user.getFirstName() + " " + user.getLastName())
                .lastUpdatedBy(user.getFirstName() + " " + user.getLastName())
                .lastUpdateDate(LocalDateTime.now())
                .build();

        MasDietType saved = repository.save(diet);

        return ResponseUtils.createSuccessResponse(convertToResponse(saved), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasDietTypeResponse> update(Long id, MasDietTypeRequest request) {

        User user = authUtil.getCurrentUser();

        Optional<MasDietType> dietOpt = repository.findById(id);
        if (dietOpt.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Diet type not found", 404);
        }

        MasDietType diet = dietOpt.get();
        diet.setDietTypeName(request.getDietTypeName());
        diet.setDescription(request.getDescription());
        diet.setLastUpdatedBy(user.getFirstName() + " " + user.getLastName());
        diet.setLastUpdateDate(LocalDateTime.now());

        MasDietType saved = repository.save(diet);

        return ResponseUtils.createSuccessResponse(convertToResponse(saved), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasDietTypeResponse> changeStatus(Long id, String status) {

        User user = authUtil.getCurrentUser();

        Optional<MasDietType> dietOpt = repository.findById(id);
        if (dietOpt.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Diet type not found", 404);
        }

        if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status must be y or n only", 400);
        }

        MasDietType diet = dietOpt.get();
        diet.setStatus(status);
        diet.setLastUpdatedBy(user.getFirstName() + " " + user.getLastName());
        diet.setLastUpdateDate(LocalDateTime.now());

        MasDietType saved = repository.save(diet);

        return ResponseUtils.createSuccessResponse(convertToResponse(saved), new TypeReference<>() {});
    }

    private MasDietTypeResponse convertToResponse(MasDietType type) {
        return new MasDietTypeResponse(
                type.getDietTypeId(),
                type.getDietTypeName(),
                type.getDescription(),
                type.getStatus(),
                type.getLastUpdateDate(),
                type.getCreatedBy(),
                type.getLastUpdatedBy()
        );
    }

}
