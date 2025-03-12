package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasApplication;
import com.hims.entity.repository.MasApplicationRepository;
import com.hims.request.MasApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasApplicationResponse;
import com.hims.service.MasApplicationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasApplicationServiceImpl implements MasApplicationService {

    @Autowired
    private MasApplicationRepository masApplicationRepository;

    @Override
    public ApiResponse<List<MasApplicationResponse>> getAllApplications(int flag) {
        List<MasApplication> applications;

        if (flag == 1) {
            applications = masApplicationRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            applications = masApplicationRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasApplicationResponse> responses = applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    public ApiResponse<MasApplicationResponse> getApplicationById(String id) {
        Optional<MasApplication> application = masApplicationRepository.findById(id);
        return application.map(value -> ResponseUtils.createSuccessResponse(convertToResponse(value), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Application not found", 404));
    }

    public ApiResponse<MasApplicationResponse> createApplication(MasApplicationRequest request) {
        MasApplication application = new MasApplication();
        application.setAppId(java.util.UUID.randomUUID().toString());
        application.setName(request.getName());
        application.setParentId(request.getParentId());
        application.setUrl(request.getUrl());
        application.setOrderNo(request.getOrderNo());
        application.setStatus("Y"); // Default status to "Y"
        application.setLastChgDate(Instant.now());
        application.setAppSequenceNo(request.getAppSequenceNo());

        MasApplication savedApplication = masApplicationRepository.save(application);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedApplication), new TypeReference<>() {});
    }

    public ApiResponse<MasApplicationResponse> updateApplication(String id, MasApplicationRequest request) {
        Optional<MasApplication> existingApplication = masApplicationRepository.findById(id);
        if (existingApplication.isPresent()) {
            MasApplication application = existingApplication.get();
            application.setName(request.getName());
            application.setParentId(request.getParentId());
            application.setUrl(request.getUrl());
            application.setOrderNo(request.getOrderNo());
            application.setAppSequenceNo(request.getAppSequenceNo());
            application.setLastChgDate(Instant.now());

            MasApplication updatedApplication = masApplicationRepository.save(application);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedApplication), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Application not found", 404);
        }
    }

    public ApiResponse<String> changeApplicationStatus(String id, String status) {
        if (!isValidStatus(status)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        Optional<MasApplication> application = masApplicationRepository.findById(id);
        if (application.isPresent()) {
            MasApplication masApplication = application.get();
            masApplication.setStatus(status);
            masApplication.setLastChgDate(Instant.now());
            masApplicationRepository.save(masApplication);
            return ResponseUtils.createSuccessResponse("Application status updated to '" + status + "'", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Application not found", 404);
        }
    }

    private boolean isValidStatus(String status) {
        return "Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status);
    }

    private MasApplicationResponse convertToResponse(MasApplication application) {
        MasApplicationResponse response = new MasApplicationResponse();
        response.setAppId(application.getAppId());
        response.setName(application.getName());
        response.setParentId(application.getParentId());
        response.setUrl(application.getUrl());
        response.setOrderNo(application.getOrderNo());
        response.setStatus(application.getStatus());
        response.setLastChgDate(application.getLastChgDate());
        response.setAppSequenceNo(application.getAppSequenceNo());
        return response;
    }
}
