package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasMaritalStatus;
import com.hims.entity.repository.MasMaritalStatusRepository;
import com.hims.request.MasMaritalStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMaritalStatusResponse;
import com.hims.service.MasMaritalStatusService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasMaritalStatusServiceImpl implements MasMaritalStatusService {

    @Autowired
    private MasMaritalStatusRepository masMaritalStatusRepository;

    @Override
    public ApiResponse<MasMaritalStatusResponse> addMaritalStatus(MasMaritalStatusRequest request) {
        MasMaritalStatus status = new MasMaritalStatus();
        status.setName(request.getName());
        status.setStatus(request.getStatus());
        status.setLastChgBy(request.getLastChgBy());
        status.setLastChgDate(Instant.now());

        MasMaritalStatus savedStatus = masMaritalStatusRepository.save(status);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedStatus), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeMaritalStatus(Long id, String statusValue) {
        Optional<MasMaritalStatus> statusOpt = masMaritalStatusRepository.findById(id);
        if (statusOpt.isPresent()) {
            MasMaritalStatus status = statusOpt.get();
            status.setStatus(statusValue);
            status.setLastChgDate(Instant.now());
            masMaritalStatusRepository.save(status);
            return ResponseUtils.createSuccessResponse("Marital status updated", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Marital status not found", 404);
        }
    }

    @Override
    public ApiResponse<MasMaritalStatusResponse> editMaritalStatus(Long id, MasMaritalStatusRequest request) {
        Optional<MasMaritalStatus> statusOpt = masMaritalStatusRepository.findById(id);
        if (statusOpt.isPresent()) {
            MasMaritalStatus status = statusOpt.get();
            status.setName(request.getName());
            status.setStatus(request.getStatus());
            status.setLastChgBy(request.getLastChgBy());
            status.setLastChgDate(Instant.now());

            masMaritalStatusRepository.save(status);
            return ResponseUtils.createSuccessResponse(mapToResponse(status), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Marital status not found", 404);
        }
    }

    @Override
    public ApiResponse<MasMaritalStatusResponse> getMaritalStatusById(Long id) {
        return masMaritalStatusRepository.findById(id)
                .map(status -> ResponseUtils.createSuccessResponse(mapToResponse(status), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Marital status not found", 404));
    }

    @Override
    public ApiResponse<List<MasMaritalStatusResponse>> getAllMaritalStatuses(int flag) {
        List<MasMaritalStatus> statuses;

        if (flag == 1) {
            statuses = masMaritalStatusRepository.findByStatusIgnoreCaseOrderByNameAsc("Y");
        } else if (flag == 0) {
            statuses = masMaritalStatusRepository.findAllByOrderByStatusDescLastChgDateDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasMaritalStatusResponse> responses = statuses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasMaritalStatusResponse mapToResponse(MasMaritalStatus status) {
        MasMaritalStatusResponse response = new MasMaritalStatusResponse();
        response.setId(status.getId());
        response.setName(status.getName());
        response.setStatus(status.getStatus());
        response.setLastChgBy(status.getLastChgBy());
        response.setLastChgDate(status.getLastChgDate());
        return response;
    }
}
