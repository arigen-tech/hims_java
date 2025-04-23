package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasApplication;
import com.hims.entity.TemplateApplication;
import com.hims.entity.repository.MasApplicationRepository;
import com.hims.entity.repository.TemplateApplicationRepository;
import com.hims.request.BatchUpdateRequest;
import com.hims.request.MasApplicationRequest;
import com.hims.request.TemplateApplicationRequest;
import com.hims.request.UpdateStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasApplicationResponse;
import com.hims.response.TemplateApplicationResponse;
import com.hims.service.MasApplicationService;
import com.hims.service.TemplateApplicationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasApplicationServiceImpl implements MasApplicationService {

    @Autowired
    private MasApplicationRepository masApplicationRepository;

    @Autowired
    private TemplateApplicationService templateApplicationService;

    @Autowired
    private TemplateApplicationRepository templateApplicationRepository;

    @Override
    public ApiResponse<List<MasApplicationResponse>> getAllApplications(int flag) {
        List<MasApplication> applications;

        if (flag == 1) {
            applications = masApplicationRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            applications = masApplicationRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<List<MasApplicationResponse>>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        // Convert entity list to response list
        List<MasApplicationResponse> responses = applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<List<MasApplicationResponse>>() {});
    }

    @Override
    public ApiResponse<MasApplicationResponse> getApplicationById(String id) {
        Optional<MasApplication> application = masApplicationRepository.findById(id);
        return application.map(value -> ResponseUtils.createSuccessResponse(convertToResponse(value), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Application not found", 404));
    }

    @Override
    public ApiResponse<MasApplicationResponse> createApplication(MasApplicationRequest request) {
        MasApplication application = new MasApplication();
        application.setName(request.getName());
        application.setParentId(request.getParentId());
        application.setUrl(request.getUrl());
        application.setStatus(request.getStatus());
        application.setLastChgDate(Instant.now());

        Long nextOrderNo = masApplicationRepository.getNextOrderNo();
        Long sequenceNo = masApplicationRepository.getNextAppSequenceNo(request.getParentId());

        application.setOrderNo(nextOrderNo);
        application.setAppId("A" + nextOrderNo);
        application.setAppSequenceNo(sequenceNo);

        MasApplication savedApplication = masApplicationRepository.save(application);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedApplication), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasApplicationResponse>> getAllByParentId(String parentId) {
        List<MasApplication> applications = masApplicationRepository.findByParentId(parentId);

        if (applications.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "No applications found for parentId: " + parentId, 404);
        }

        List<MasApplicationResponse> responses = applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    @Override
    public ApiResponse<MasApplicationResponse> updateApplication(String id, MasApplicationRequest request) {
        Optional<MasApplication> existingApplication = masApplicationRepository.findById(id);
        if (existingApplication.isPresent()) {
            MasApplication application = existingApplication.get();
            application.setName(request.getName());
            application.setParentId(request.getParentId());
            application.setUrl(request.getUrl());
            application.setStatus(request.getStatus());
            application.setLastChgDate(Instant.now());

            MasApplication updatedApplication = masApplicationRepository.save(application);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedApplication), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Application not found", 404);
        }
    }

    @Override
    public ApiResponse<String> updateMultipleApplicationStatuses(UpdateStatusRequest request) {
        List<UpdateStatusRequest.ApplicationStatusUpdate> updates = request.getApplications();

        if (updates == null || updates.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "No applications provided for update.", 400);
        }

        List<String> appIds = updates.stream().map(UpdateStatusRequest.ApplicationStatusUpdate::getAppId).toList();
        List<MasApplication> applications = masApplicationRepository.findAllById(appIds);

        if (applications.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "No matching applications found for the given IDs.", 404);
        }

        Map<String, String> statusMap = updates.stream()
                .collect(Collectors.toMap(UpdateStatusRequest.ApplicationStatusUpdate::getAppId, UpdateStatusRequest.ApplicationStatusUpdate::getStatus));

        applications.forEach(app -> {
            String newStatus = statusMap.get(app.getAppId());
            if (newStatus != null && isValidStatus(newStatus)) {
                app.setStatus(newStatus);
                app.setLastChgDate(Instant.now());
            }
        });

        masApplicationRepository.saveAll(applications);

        return ResponseUtils.createSuccessResponse("Successfully updated " + applications.size() + " applications.", new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> processBatchUpdates(BatchUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        // Validate application status updates
        if (request.getApplicationStatusUpdates() != null) {
            // Remove any null or empty entries
            request.setApplicationStatusUpdates(
                    request.getApplicationStatusUpdates().stream()
                            .filter(update ->
                                    StringUtils.hasText(update.getAppId()) &&
                                            StringUtils.hasText(update.getStatus()))
                            .collect(Collectors.toList())
            );

            // Validate status values and collect specific errors
            for (BatchUpdateRequest.ApplicationStatusUpdate update : request.getApplicationStatusUpdates()) {
                if (!isValidStatus(update.getStatus())) {
                    errors.add("Invalid status '" + update.getStatus() + "' for app " + update.getAppId());
                }
            }
        }

        // Validate template application assignments
        if (request.getTemplateApplicationAssignments() != null) {
            // Remove any null or incomplete entries
            request.setTemplateApplicationAssignments(
                    request.getTemplateApplicationAssignments().stream()
                            .filter(assignment ->
                                    assignment.getTemplateId() != null &&
                                            StringUtils.hasText(assignment.getAppId()) &&
                                            assignment.getLastChgBy() != null)
                            .collect(Collectors.toList())
            );
        }

        // If there are validation errors, return failure response
        if (!errors.isEmpty()) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Validation errors: " + String.join(", ", errors),
                    400
            );
        }

        // Validate input
        if ((request.getApplicationStatusUpdates() == null || request.getApplicationStatusUpdates().isEmpty()) &&
                (request.getTemplateApplicationAssignments() == null || request.getTemplateApplicationAssignments().isEmpty())) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "No valid updates or assignments provided.",
                    400
            );
        }

        // Process application status updates
        List<String> statusUpdateMessages = new ArrayList<>();
        if (request.getApplicationStatusUpdates() != null && !request.getApplicationStatusUpdates().isEmpty()) {
            // Collect all application IDs to fetch in a single query
            List<String> appIds = request.getApplicationStatusUpdates().stream()
                    .map(BatchUpdateRequest.ApplicationStatusUpdate::getAppId)
                    .collect(Collectors.toList());

            // Fetch all applications in a single database query
            List<MasApplication> applications = masApplicationRepository.findAllById(appIds);

            // Create a map for faster lookup
            Map<String, MasApplication> applicationMap = applications.stream()
                    .collect(Collectors.toMap(MasApplication::getAppId, app -> app));

            // Process each update
            List<MasApplication> updatedApplications = new ArrayList<>();
            for (BatchUpdateRequest.ApplicationStatusUpdate update : request.getApplicationStatusUpdates()) {
                MasApplication application = applicationMap.get(update.getAppId());
                if (application != null) {
                    application.setStatus(update.getStatus().toLowerCase());
                    application.setLastChgDate(Instant.now());
                    updatedApplications.add(application);
                } else {
                    errors.add("Application not found: " + update.getAppId());
                }
            }

            // Check for any not found applications
            if (!errors.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Errors: " + String.join(", ", errors),
                        404
                );
            }

            // Save all updated applications
            masApplicationRepository.saveAll(updatedApplications);
            statusUpdateMessages.add("Successfully updated " + updatedApplications.size() + " applications.");
        }

        // Process template application assignments (rest of the method remains the same)
        List<String> templateAssignmentMessages = new ArrayList<>();
        if (request.getTemplateApplicationAssignments() != null && !request.getTemplateApplicationAssignments().isEmpty()) {
            List<String> successfulAssignments = new ArrayList<>();
            List<String> skippedAssignments = new ArrayList<>();
            List<String> failedAssignments = new ArrayList<>();

            for (BatchUpdateRequest.TemplateApplicationAssignment assignment : request.getTemplateApplicationAssignments()) {
                // Check if template is already assigned to the application
                Optional<TemplateApplication> existingAssignment =
                        templateApplicationRepository.findByTemplate_IdAndApp_AppId(
                                assignment.getTemplateId(),
                                assignment.getAppId()
                        );

                if (existingAssignment.isPresent()) {
                    // Skip if already assigned
                    skippedAssignments.add(assignment.getAppId());
                    continue;
                }

                // If not assigned, proceed with assignment
                TemplateApplicationRequest templateRequest = new TemplateApplicationRequest();
                templateRequest.setTemplateId(assignment.getTemplateId());
                templateRequest.setAppId(assignment.getAppId());
                templateRequest.setLastChgBy(assignment.getLastChgBy());
                templateRequest.setOrderNo(assignment.getOrderNo());

                try {
                    ApiResponse<TemplateApplicationResponse> assignmentResponse =
                            templateApplicationService.assignTemplateToApplication(templateRequest);

                    if (assignmentResponse.getStatus() == 200) {
                        successfulAssignments.add(assignment.getAppId());
                    } else {
                        failedAssignments.add(assignment.getAppId());
                    }
                } catch (Exception e) {
                    failedAssignments.add(assignment.getAppId());
                }
            }

            // Prepare template assignment message
            if (!successfulAssignments.isEmpty()) {
                templateAssignmentMessages.add("Successful template assignments: " + successfulAssignments);
            }
            if (!skippedAssignments.isEmpty()) {
                templateAssignmentMessages.add("Skipped template assignments (already exists): " + skippedAssignments);
            }
            if (!failedAssignments.isEmpty()) {
                templateAssignmentMessages.add("Failed template assignments: " + failedAssignments);
            }
        }

        // Combine messages
        List<String> allMessages = new ArrayList<>();
        allMessages.addAll(statusUpdateMessages);
        allMessages.addAll(templateAssignmentMessages);

        String finalMessage = String.join(". ", allMessages);
        return ResponseUtils.createSuccessResponse(
                finalMessage.isEmpty() ? "No updates processed" : finalMessage,
                new TypeReference<>() {}
        );
    }

    @Override
    public ApiResponse<List<MasApplicationResponse>> getAllParentApplications(int flag) {
        List<MasApplication> applications;

        // First get applications based on status flag
        if (flag == 1) {
            applications = masApplicationRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            applications = masApplicationRepository.findAll();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        // Filter to only get parent applications (where parentId is "0")
        applications = applications.stream()
                .filter(app -> app.getParentId() != null && app.getParentId().equals("0"))
                .collect(Collectors.toList());

        List<MasApplicationResponse> responses = applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
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
