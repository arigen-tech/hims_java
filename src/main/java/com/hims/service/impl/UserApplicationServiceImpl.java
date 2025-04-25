package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.User;
import com.hims.entity.UserApplication;
import com.hims.entity.repository.UserApplicationRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.UserApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.UserApplicationResponse;
import com.hims.service.UserApplicationService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

    private static final Logger log = LoggerFactory.getLogger(DoctorRosterServicesImpl.class);

    @Autowired
    private UserApplicationRepository userApplicationRepository;
    @Autowired
    UserRepo userRepo;

    @Override
    public ApiResponse<List<UserApplicationResponse>> getAllApplications(int flag) {
        List<UserApplication> applications;

        if (flag == 1) {
            applications = userApplicationRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            applications = userApplicationRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

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

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }


        UserApplication application = new UserApplication();
        application.setUserAppName(request.getUserAppName());
        application.setUrl(request.getUrl());
        application.setStatus("y"); // Default status to "Y"
        application.setLastChgBy(currentUser.getUserId());
        application.setLastChgDate(Instant.now());

        UserApplication savedApplication = userApplicationRepository.save(application);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedApplication), new TypeReference<>() {});
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
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

    public ApiResponse<List<UserApplicationResponse>> getAllApplicationsWithHashUrl(int flag) {
        List<UserApplication> applications;

        if (flag == 1) {
            applications = userApplicationRepository.findByStatusIgnoreCaseAndUrl("Y", "#");
        } else if (flag == 0) {
            applications = userApplicationRepository.findByStatusInIgnoreCaseAndUrl(List.of("Y", "N"), "#");
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<UserApplicationResponse> responses = applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
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
