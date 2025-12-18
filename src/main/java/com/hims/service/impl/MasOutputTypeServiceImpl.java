package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasOutputType;
import com.hims.entity.User;
import com.hims.entity.repository.MasOutputTypeRepository;
import com.hims.request.MasOutputTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOutputTypeResponse;
import com.hims.service.MasOutputTypeService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasOutputTypeServiceImpl implements MasOutputTypeService {

    @Autowired
    private MasOutputTypeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasOutputTypeResponse>> getAll(int flag) {
        try {
            List<MasOutputType> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByOutputTypeNameAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            List<MasOutputTypeResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasOutputTypeResponse> getById(Long id) {
        try {
            MasOutputType output = repository.findById(id).orElse(null);

            if (output == null)
                return ResponseUtils.createNotFoundResponse("Output Type ID not found!", 404);

            return ResponseUtils.createSuccessResponse(toResponse(output), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasOutputTypeResponse> create(MasOutputTypeRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasOutputType output = MasOutputType.builder()
                    .outputTypeName(request.getOutputTypeName())
                    .isMeasurable(request.getIsMeasurable())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            MasOutputType saved = repository.save(output);

            return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Creation failed: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasOutputTypeResponse> update(Long id, MasOutputTypeRequest request) {
        try {
            MasOutputType output = repository.findById(id).orElse(null);

            if (output == null)
                return ResponseUtils.createNotFoundResponse("Output Type ID not found!", 404);

            User user = authUtil.getCurrentUser();

            output.setOutputTypeName(request.getOutputTypeName());
            output.setIsMeasurable(request.getIsMeasurable());
            output.setDescription(request.getDescription());
            output.setLastUpdatedBy(user.getFirstName());
            output.setLastUpdateDate(LocalDateTime.now());

            repository.save(output);

            return ResponseUtils.createSuccessResponse(toResponse(output), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Update failed: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasOutputTypeResponse> changeStatus(Long id, String status) {
        try {
            MasOutputType output = repository.findById(id).orElse(null);

            if (output == null)
                return ResponseUtils.createNotFoundResponse("Output Type ID not found!", 404);

            if (!status.equals("y") && !status.equals("n"))
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status!",
                        400
                );

            User user = authUtil.getCurrentUser();

            output.setStatus(status);
            output.setLastUpdatedBy(user.getFirstName());
            output.setLastUpdateDate(LocalDateTime.now());

            repository.save(output);

            return ResponseUtils.createSuccessResponse(toResponse(output), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Status update failed: " + e.getMessage(),
                    500
            );
        }
    }

    private MasOutputTypeResponse toResponse(MasOutputType m) {
        return new MasOutputTypeResponse(
                m.getOutputTypeId(),
                m.getOutputTypeName(),
                m.getIsMeasurable(),
                m.getStatus(),
                m.getLastUpdateDate(),
                m.getCreatedBy(),
                m.getLastUpdatedBy(),
                m.getDescription()
        );
    }

}
