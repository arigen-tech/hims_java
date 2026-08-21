package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasAnaesthesiaInstruction;
import com.hims.entity.User;
import com.hims.entity.repository.MasAnaesthesiaInstructionRepository;
import com.hims.request.MasAnaesthesiaInstructionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAnaesthesiaInstructionResponse;
import com.hims.service.MasAnaesthesiaInstructionService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.hims.constants.AppConstants.STATUS_N;
import static com.hims.constants.AppConstants.STATUS_Y;

@Service
public class MasAnaesthesiaInstructionServiceImpl implements MasAnaesthesiaInstructionService {

    private static final String INSTRUCTION_TYPE_PRE = "PRE";
    private static final String INSTRUCTION_TYPE_POST = "POST";

    @Autowired
    private MasAnaesthesiaInstructionRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasAnaesthesiaInstructionResponse>> getAll(int flag) {
        try {
            List<MasAnaesthesiaInstruction> list =
                    (flag == 1) ? repository.findByStatusIgnoreCaseOrderByInstructionTypeAscInstructionAsc(STATUS_Y)
                            : repository.findAllByOrderByStatusDescLastChgDateDesc();

            List<MasAnaesthesiaInstructionResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasAnaesthesiaInstructionResponse> getById(Long id) {
        try {
            MasAnaesthesiaInstruction obj = repository.findById(id).orElse(null);

            if (obj == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", HttpStatus.NOT_FOUND.value());

            return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasAnaesthesiaInstructionResponse> create(MasAnaesthesiaInstructionRequest request) {
        try {
            String instructionType = normalizeInstructionType(request.getInstructionType());
            if (instructionType == null) {
                return invalidInstructionTypeResponse();
            }

            User user = authUtil.getCurrentUser();

            MasAnaesthesiaInstruction data = MasAnaesthesiaInstruction.builder()
                    .instructionType(instructionType)
                    .instruction(request.getInstruction())
                    .status(STATUS_Y)
                    .lastChgBy(user.getFirstName())
                    .lastChgDate(LocalDateTime.now())
                    .build();

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasAnaesthesiaInstructionResponse> update(Long id, MasAnaesthesiaInstructionRequest request) {
        try {
            MasAnaesthesiaInstruction data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", HttpStatus.NOT_FOUND.value());

            String instructionType = normalizeInstructionType(request.getInstructionType());
            if (instructionType == null) {
                return invalidInstructionTypeResponse();
            }

            User user = authUtil.getCurrentUser();

            data.setInstructionType(instructionType);
            data.setInstruction(request.getInstruction());
            data.setLastChgBy(user.getFirstName());
            data.setLastChgDate(LocalDateTime.now());

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasAnaesthesiaInstructionResponse> changeStatus(Long id, String status) {
        try {
            MasAnaesthesiaInstruction data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", HttpStatus.NOT_FOUND.value());

            if (!status.equalsIgnoreCase(STATUS_Y) && !status.equalsIgnoreCase(STATUS_N))
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid Status!", HttpStatus.BAD_REQUEST.value());

            User user = authUtil.getCurrentUser();

            data.setStatus(status.toUpperCase());
            data.setLastChgBy(user.getFirstName());
            data.setLastChgDate(LocalDateTime.now());

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String normalizeInstructionType(String instructionType) {
        if (instructionType == null) {
            return null;
        }
        String normalized = instructionType.toUpperCase();
        if (!normalized.equals(INSTRUCTION_TYPE_PRE) && !normalized.equals(INSTRUCTION_TYPE_POST)) {
            return null;
        }
        return normalized;
    }

    private ApiResponse<MasAnaesthesiaInstructionResponse> invalidInstructionTypeResponse() {
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                "Invalid Instruction Type!", HttpStatus.BAD_REQUEST.value());
    }

    private MasAnaesthesiaInstructionResponse toResponse(MasAnaesthesiaInstruction m) {
        return new MasAnaesthesiaInstructionResponse(
                m.getAnaesthesiaInstructionId(),
                m.getInstructionType(),
                m.getInstruction(),
                m.getStatus(),
                m.getLastChgBy(),
                m.getLastChgDate()
        );
    }
}
