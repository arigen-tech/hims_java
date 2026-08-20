package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasOtScheduleChangeReason;
import com.hims.entity.User;
import com.hims.entity.repository.MasOtScheduleChangeReasonRepository;
import com.hims.request.MasOtScheduleChangeReasonRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtScheduleChangeReasonResponse;
import com.hims.service.MasOtScheduleChangeReasonService;
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
public class MasOtScheduleChangeReasonServiceImpl implements MasOtScheduleChangeReasonService {

    private static final String APPLICABLE_FOR_CANCEL = "CANCEL";
    private static final String APPLICABLE_FOR_RESCHEDULE = "RESCHEDULE";
    private static final String APPLICABLE_FOR_BOTH = "BOTH";

    @Autowired
    private MasOtScheduleChangeReasonRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasOtScheduleChangeReasonResponse>> getAll(int flag) {
        try {
            List<MasOtScheduleChangeReason> list =
                    (flag == 1) ? repository.findByStatusIgnoreCaseOrderByApplicableForAscReasonAsc(STATUS_Y)
                            : repository.findAllByOrderByStatusDescLastChgDateDesc();

            List<MasOtScheduleChangeReasonResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasOtScheduleChangeReasonResponse> getById(Long id) {
        try {
            MasOtScheduleChangeReason obj = repository.findById(id).orElse(null);

            if (obj == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", HttpStatus.NOT_FOUND.value());

            return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasOtScheduleChangeReasonResponse> create(MasOtScheduleChangeReasonRequest request) {
        try {
            String applicableFor = normalizeApplicableFor(request.getApplicableFor());
            if (applicableFor == null) {
                return invalidApplicableForResponse();
            }

            User user = authUtil.getCurrentUser();

            MasOtScheduleChangeReason data = MasOtScheduleChangeReason.builder()
                    .reason(request.getReason())
                    .applicableFor(applicableFor)
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
    public ApiResponse<MasOtScheduleChangeReasonResponse> update(Long id, MasOtScheduleChangeReasonRequest request) {
        try {
            MasOtScheduleChangeReason data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", HttpStatus.NOT_FOUND.value());

            String applicableFor = normalizeApplicableFor(request.getApplicableFor());
            if (applicableFor == null) {
                return invalidApplicableForResponse();
            }

            User user = authUtil.getCurrentUser();

            data.setReason(request.getReason());
            data.setApplicableFor(applicableFor);
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
    public ApiResponse<MasOtScheduleChangeReasonResponse> changeStatus(Long id, String status) {
        try {
            MasOtScheduleChangeReason data = repository.findById(id).orElse(null);

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

    private String normalizeApplicableFor(String applicableFor) {
        if (applicableFor == null) {
            return null;
        }
        String normalized = applicableFor.toUpperCase();
        if (!normalized.equals(APPLICABLE_FOR_CANCEL)
                && !normalized.equals(APPLICABLE_FOR_RESCHEDULE)
                && !normalized.equals(APPLICABLE_FOR_BOTH)) {
            return null;
        }
        return normalized;
    }

    private ApiResponse<MasOtScheduleChangeReasonResponse> invalidApplicableForResponse() {
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                "Invalid Applicable For!", HttpStatus.BAD_REQUEST.value());
    }

    private MasOtScheduleChangeReasonResponse toResponse(MasOtScheduleChangeReason m) {
        return new MasOtScheduleChangeReasonResponse(
                m.getReasonId(),
                m.getReason(),
                m.getApplicableFor(),
                m.getStatus(),
                m.getLastChgBy(),
                m.getLastChgDate()
        );
    }
}
