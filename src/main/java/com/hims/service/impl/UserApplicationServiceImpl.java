package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.UserApplication;
import com.hims.entity.repository.UserApplicationRepository;
import com.hims.request.UserApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.UserApplicationResponse;
import com.hims.service.UserApplicationService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

    @Autowired
    private UserApplicationRepository userApplicationRepository;

    public ApiResponse<List<UserApplicationResponse>> getAllApplications() {
        List<UserApplication> applications = userApplicationRepository.findAll();
        List<UserApplicationResponse> responses = applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    public ApiResponse<UserApplicationResponse> getApplicationById(Long id) {
        Optional<UserApplication> application = userApplicationRepository.findById(id);
        return application.map(value -> ResponseUtils.createSuccessResponse(convertToResponse(value), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Application not found", 404));
    }

    public ApiResponse<UserApplicationResponse> createApplication(UserApplicationRequest request) {
        UserApplication application = new UserApplication();
        application.setUserAppName(request.getUserAppName());
        application.setUrl(request.getUrl());
        application.setStatus("y"); // Default status to "Y"
        application.setLastChgBy(request.getLastChgBy());
        application.setLastChgDate(Instant.now());

        UserApplication savedApplication = userApplicationRepository.save(application);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedApplication), new TypeReference<>() {});
    }

    public ApiResponse<UserApplicationResponse> updateApplication(Long id, UserApplicationRequest request) {
        Optional<UserApplication> existingApplication = userApplicationRepository.findById(id);
        if (existingApplication.isPresent()) {
            UserApplication application = existingApplication.get();
            application.setUserAppName(request.getUserAppName());
            application.setUrl(request.getUrl());
            application.setLastChgBy(request.getLastChgBy());
            application.setLastChgDate(Instant.now());

            UserApplication updatedApplication = userApplicationRepository.save(application);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedApplication), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Application not found", 404);
        }
    }

    public ApiResponse<String> changeApplicationStatus(Long id, String status) {
        if (!isValidStatus(status)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        Optional<UserApplication> application = userApplicationRepository.findById(id);
        if (application.isPresent()) {
            UserApplication userApplication = application.get();
            userApplication.setStatus(status);
            userApplication.setLastChgDate(Instant.now());
            userApplicationRepository.save(userApplication);
            return ResponseUtils.createSuccessResponse("Application status updated to '" + status + "'", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Application not found", 404);
        }
    }

    private boolean isValidStatus(String status) {
        return "y".equalsIgnoreCase(status) || "n".equalsIgnoreCase(status);
    }

    private UserApplicationResponse convertToResponse(UserApplication application) {
        UserApplicationResponse response = new UserApplicationResponse();
        response.setId(application.getId());
        response.setUserAppName(application.getUserAppName());
        response.setUrl(application.getUrl());
        response.setStatus(application.getStatus());
        response.setLastChgBy(application.getLastChgBy());
        response.setLastChgDate(application.getLastChgDate());
        return response;
    }
}
