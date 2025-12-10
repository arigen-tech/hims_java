package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasIntakeItem;
import com.hims.entity.MasIntakeType;
import com.hims.entity.User;
import com.hims.entity.repository.MasIntakeItemRepository;
import com.hims.entity.repository.MasIntakeTypeRepository;
import com.hims.request.MasIntakeItemRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasIntakeItemResponse;
import com.hims.service.MasIntakeItemService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Builder
public class MasIntakeItemServiceImpl implements MasIntakeItemService {

    @Autowired
    private MasIntakeItemRepository repository;

    @Autowired
    private MasIntakeTypeRepository typeRepository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasIntakeItemResponse>> getAll(int flag) {
        try {
            List<MasIntakeItem> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByLastUpdateDateDesc("y")
                            : repository.findAllByOrderByLastUpdateDateDesc();

            List<MasIntakeItemResponse> response = list.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasIntakeItemResponse> getById(Long id) {
        try {
            MasIntakeItem item = repository.findById(id).orElse(null);

            if (item == null)
                return ResponseUtils.createNotFoundResponse("Intake Item not found!", 404);

            return ResponseUtils.createSuccessResponse(toResponse(item), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasIntakeItemResponse> create(MasIntakeItemRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasIntakeType type = typeRepository.findById(request.getIntakeTypeId())
                    .orElseThrow(() -> new RuntimeException("Invalid intake type"));

            MasIntakeItem item = MasIntakeItem.builder()
                    .intakeType(type)
                    .intakeItemName(request.getIntakeItemName())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            MasIntakeItem saved = repository.save(item);

            return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasIntakeItemResponse> update(Long id, MasIntakeItemRequest request) {
        try {
            MasIntakeItem item = repository.findById(id).orElse(null);

            if (item == null)
                return ResponseUtils.createNotFoundResponse("Intake Item not found!", 404);

            MasIntakeType type = typeRepository.findById(request.getIntakeTypeId())
                    .orElseThrow(() -> new RuntimeException("Invalid intake type"));

            User user = authUtil.getCurrentUser();

            item.setIntakeType(type);
            item.setIntakeItemName(request.getIntakeItemName());
            item.setLastUpdatedBy(user.getFirstName());
            item.setLastUpdateDate(LocalDateTime.now());

            repository.save(item);

            return ResponseUtils.createSuccessResponse(toResponse(item), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasIntakeItemResponse> changeStatus(Long id, String status) {
        try {
            MasIntakeItem item = repository.findById(id).orElse(null);

            if (item == null)
                return ResponseUtils.createNotFoundResponse("Intake Item not found!", 404);

            if (!status.equals("y") && !status.equals("n"))
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {}, "Invalid status!", 400);

            User user = authUtil.getCurrentUser();

            item.setStatus(status);
            item.setLastUpdatedBy(user.getFirstName());
            item.setLastUpdateDate(LocalDateTime.now());

            repository.save(item);

            return ResponseUtils.createSuccessResponse(toResponse(item), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed: " + e.getMessage(), 500);
        }
    }

    private MasIntakeItemResponse toResponse(MasIntakeItem i) {
        return new MasIntakeItemResponse(
                i.getIntakeItemId(),
                i.getIntakeType().getIntakeTypeId(),
                i.getIntakeType().getIntakeTypeName(),
                i.getIntakeItemName(),
                i.getStatus(),
                i.getLastUpdateDate(),
                i.getCreatedBy(),
                i.getLastUpdatedBy()
        );
    }

}
