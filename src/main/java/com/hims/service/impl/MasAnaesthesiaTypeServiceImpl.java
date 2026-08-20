package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasAnaesthesiaType;
import com.hims.entity.User;
import com.hims.entity.repository.MasAnaesthesiaTypeRepository;
import com.hims.request.MasAnaesthesiaTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAnaesthesiaTypeResponse;
import com.hims.service.MasAnaesthesiaTypeService;
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
public class MasAnaesthesiaTypeServiceImpl implements MasAnaesthesiaTypeService {

    @Autowired
    private MasAnaesthesiaTypeRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasAnaesthesiaTypeResponse>> getAll(int flag) {
        try {
            List<MasAnaesthesiaType> list =
                    (flag == 1) ? repository.findByStatusIgnoreCaseOrderByAnaesthesiaTypeNameAsc(STATUS_Y.toLowerCase())
                            : repository.findAllByOrderByStatusDescLastChgDateDesc();

            List<MasAnaesthesiaTypeResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasAnaesthesiaTypeResponse> getById(Long id) {
        try {
            MasAnaesthesiaType obj = repository.findById(id).orElse(null);

            if (obj == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", HttpStatus.NOT_FOUND.value());

            return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasAnaesthesiaTypeResponse> create(MasAnaesthesiaTypeRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasAnaesthesiaType data = MasAnaesthesiaType.builder()
                    .anaesthesiaTypeCode(request.getAnaesthesiaTypeCode())
                    .anaesthesiaTypeName(request.getAnaesthesiaTypeName())
                    .status(STATUS_Y)
                    .lastChgBy(user.getFirstName())
                    .lastChgDate(LocalDateTime.now())
                    .build();

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasAnaesthesiaTypeResponse> update(Long id, MasAnaesthesiaTypeRequest request) {
        try {
            MasAnaesthesiaType data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", HttpStatus.NOT_FOUND.value());

            User user = authUtil.getCurrentUser();

            data.setAnaesthesiaTypeCode(request.getAnaesthesiaTypeCode());
            data.setAnaesthesiaTypeName(request.getAnaesthesiaTypeName());
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
    public ApiResponse<MasAnaesthesiaTypeResponse> changeStatus(Long id, String status) {
        try {
            MasAnaesthesiaType data = repository.findById(id).orElse(null);

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

    private MasAnaesthesiaTypeResponse toResponse(MasAnaesthesiaType m) {
        return new MasAnaesthesiaTypeResponse(
                m.getAnaesthesiaTypeId(),
                m.getAnaesthesiaTypeCode(),
                m.getAnaesthesiaTypeName(),
                m.getStatus(),
                m.getLastChgBy(),
                m.getLastChgDate()
        );
    }
}
