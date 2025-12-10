package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasMealType;
import com.hims.entity.User;
import com.hims.entity.repository.MasMealTypeRepository;
import com.hims.request.MasMealTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMealTypeResponse;
import com.hims.service.MasMealTypeService;
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
public class MasMealTypeServiceImpl implements MasMealTypeService {
    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private MasMealTypeRepository repository;

    @Override
    public ApiResponse<List<MasMealTypeResponse>> getAllMealType(int flag) {

        log.info("MasMealType: Fetch All Start | flag={}", flag);

        try {
            List<MasMealType> list;

            if (flag == 1) {
                list = repository.findByStatusIgnoreCase("y");
            } else if (flag == 0) {
                list = repository.findAll();
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value. Use 0 or 1.", 400);
            }

            List<MasMealTypeResponse> responses = list.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("MasMealType: Error - {}", e.getMessage());
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Unexpected error: " + e.getMessage(),
                    500);
        }
    }

    @Override
    public ApiResponse<MasMealTypeResponse> findById(Long id) {

        Optional<MasMealType> meal = repository.findById(id);

        if (meal.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Meal type not found", 404);
        }

        return ResponseUtils.createSuccessResponse(convertToResponse(meal.get()),
                new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasMealTypeResponse> addMealType(MasMealTypeRequest request) {

        User user = authUtil.getCurrentUser();
        if (user == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", 400);
        }

        MasMealType type = MasMealType.builder()
                .mealTypeName(request.getMealTypeName())
                .sequenceNo(request.getSequenceNo())
                .status("y")
                .createdBy(user.getFirstName() + " " + user.getFirstName())
                .lastUpdatedBy(user.getFirstName() + " " + user.getFirstName())
                .lastUpdateDate(LocalDateTime.now())
                .build();

        MasMealType saved = repository.save(type);

        return ResponseUtils.createSuccessResponse(convertToResponse(saved), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasMealTypeResponse> update(Long id, MasMealTypeRequest request) {

        User user = authUtil.getCurrentUser();

        Optional<MasMealType> mealOpt = repository.findById(id);
        if (mealOpt.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Meal type not found", 404);
        }

        MasMealType meal = mealOpt.get();
        meal.setMealTypeName(request.getMealTypeName());
        meal.setSequenceNo(request.getSequenceNo());
        meal.setLastUpdatedBy(user.getFirstName() + " " + user.getLastName());
        meal.setLastUpdateDate(LocalDateTime.now());

        MasMealType saved = repository.save(meal);

        return ResponseUtils.createSuccessResponse(convertToResponse(saved), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasMealTypeResponse> changeStatus(Long id, String status) {

        User user = authUtil.getCurrentUser();

        Optional<MasMealType> mealOpt = repository.findById(id);
        if (mealOpt.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Meal type not found", 404);
        }

        if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status must be y or n only", 400);
        }

        MasMealType meal = mealOpt.get();
        meal.setStatus(status.toUpperCase());
        meal.setLastUpdatedBy(user.getFirstName() + " " + user.getLastName());
        meal.setLastUpdateDate(LocalDateTime.now());

        MasMealType saved = repository.save(meal);

        return ResponseUtils.createSuccessResponse(convertToResponse(saved), new TypeReference<>() {});
    }

    private MasMealTypeResponse convertToResponse(MasMealType type) {
        return new MasMealTypeResponse(
                type.getMealTypeId(),
                type.getMealTypeName(),
                type.getSequenceNo(),
                type.getStatus(),
                type.getLastUpdateDate(),
                type.getCreatedBy(),
                type.getLastUpdatedBy()
        );
    }


}
