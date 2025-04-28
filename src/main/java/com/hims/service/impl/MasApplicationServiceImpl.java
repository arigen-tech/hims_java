package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasApplication;
import com.hims.entity.TemplateApplication;
import com.hims.entity.UserApplication;
import com.hims.entity.repository.MasApplicationRepository;
import com.hims.entity.repository.TemplateApplicationRepository;
import com.hims.entity.repository.UserApplicationRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasApplicationServiceImpl implements MasApplicationService {

    @Autowired
    private MasApplicationRepository masApplicationRepository;

    @Autowired
    private TemplateApplicationService templateApplicationService;

    @Autowired
    private TemplateApplicationRepository templateApplicationRepository;

    @Autowired
    private UserApplicationRepository userApplicationRepository;

    private static final Logger log = LoggerFactory.getLogger(DoctorRosterServicesImpl.class);

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
        // Create new MasApplication
        MasApplication application = new MasApplication();
        application.setName(request.getName());
        application.setParentId(request.getParentId());
        application.setUrl(request.getUrl());
        application.setStatus(request.getStatus()); // Keep original status logic for mas_application
        application.setLastChgDate(Instant.now());

        Long nextOrderNo = masApplicationRepository.getNextOrderNo();
        Long sequenceNo = masApplicationRepository.getNextAppSequenceNo(request.getParentId());

        application.setOrderNo(nextOrderNo);
        application.setAppId("A" + nextOrderNo);
        application.setAppSequenceNo(sequenceNo);

        MasApplication savedApplication = masApplicationRepository.save(application);

        // Find and update the existing record in user_applications table by name
        UserApplication existingUserApp = userApplicationRepository.findByUserAppName(request.getName());
        if (existingUserApp != null) {
            // Only update the status to "n", keep other fields unchanged
            existingUserApp.setStatus("n");
            existingUserApp.setLastChgDate(Instant.now());
            // If you have lastChgBy field, update it here
            // existingUserApp.setLastChgBy(getCurrentUserId());

            userApplicationRepository.save(existingUserApp);
        }

        return ResponseUtils.createSuccessResponse(convertToResponse(savedApplication), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasApplicationResponse>> getAllByParentId(String parentId, Long templateId) {
        // First, fetch the parent application
        MasApplication parentApplication = masApplicationRepository.findById(parentId)
                .orElse(null);

        if (parentApplication == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Parent application not found with id: " + parentId, 404);
        }

        // Then fetch all child applications
        List<MasApplication> childApplications = masApplicationRepository.findByParentId(parentId);

        // Create a combined list starting with the parent
        List<MasApplicationResponse> responses = new ArrayList<>();

        // Add parent first with template status
        responses.add(convertToResponseWithTemplateStatus(parentApplication, templateId));

        // Add all children with template status
        childApplications.forEach(child ->
                responses.add(convertToResponseWithTemplateStatus(child, templateId)));

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasApplicationResponse convertToResponseWithTemplateStatus(MasApplication application, Long templateId) {
        MasApplicationResponse response = convertToResponse(application);

        // Check if this application is assigned to the template
        TemplateApplication templateApp = templateApplicationRepository.findByTemplateAndApp(templateId, application.getAppId())
                .orElse(null);

        if (templateApp != null) {
            response.setAssigned(true);
            response.setStatus(templateApp.getStatus()); // "Y" or "N"
        } else {
            response.setAssigned(false);
            response.setStatus("N"); // Default to "N" if not assigned
        }

        return response;
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

        // Validate inputs
        if (request.getTemplateApplicationAssignments() != null) {
            request.setTemplateApplicationAssignments(
                    request.getTemplateApplicationAssignments().stream()
                            .filter(assignment ->
                                    assignment.getTemplateId() != null &&
                                            StringUtils.hasText(assignment.getAppId()) &&
                                            assignment.getLastChgBy() != null)
                            .collect(Collectors.toList())
            );
        }

        if (request.getApplicationStatusUpdates() != null) {
            request.setApplicationStatusUpdates(
                    request.getApplicationStatusUpdates().stream()
                            .filter(update ->
                                    StringUtils.hasText(update.getAppId()) &&
                                            StringUtils.hasText(update.getStatus()))
                            .collect(Collectors.toList())
            );
        }

        // Validate input
        if ((request.getTemplateApplicationAssignments() == null ||
                request.getTemplateApplicationAssignments().isEmpty()) &&
                (request.getApplicationStatusUpdates() == null ||
                        request.getApplicationStatusUpdates().isEmpty())) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "No valid updates or assignments provided.",
                    400
            );
        }

        List<String> successMessages = new ArrayList<>();

        // Process status updates first to ensure we have the map of updates
        Map<String, String> statusUpdates = new HashMap<>();
        if (request.getApplicationStatusUpdates() != null) {
            for (BatchUpdateRequest.ApplicationStatusUpdate update : request.getApplicationStatusUpdates()) {
                String appId = update.getAppId();
                String status = update.getStatus().toLowerCase();
                statusUpdates.put(appId, status);

                // Log status update for debugging
                log.info("Status update request: appId={}, status={}", appId, status);
            }
        }

        // Process template application assignments
        List<String> successfulAssignments = new ArrayList<>();
        List<String> skippedAssignments = new ArrayList<>();
        List<String> failedAssignments = new ArrayList<>();

        if (request.getTemplateApplicationAssignments() != null &&
                !request.getTemplateApplicationAssignments().isEmpty()) {
            // Track newly created assignments
            Set<String> newlyCreatedApps = new HashSet<>();

            for (BatchUpdateRequest.TemplateApplicationAssignment assignment :
                    request.getTemplateApplicationAssignments()) {
                String appId = assignment.getAppId();
                Long templateId = assignment.getTemplateId();

                // Check if template is already assigned to the application
                Optional<TemplateApplication> existingAssignment =
                        templateApplicationRepository.findByTemplate_IdAndApp_AppId(
                                templateId,
                                appId
                        );

                if (existingAssignment.isPresent()) {
                    TemplateApplication templateApp = existingAssignment.get();

                    // Update existing template application
                    if (assignment.getOrderNo() != null) {
                        templateApp.setOrderNo(assignment.getOrderNo());
                    }

                    // Apply status update if available
                    if (statusUpdates.containsKey(appId)) {
                        templateApp.setStatus(statusUpdates.get(appId));
                        // Remove from status updates as we've already applied it
                        statusUpdates.remove(appId);
                    } else if (assignment.getStatus() != null) {
                        templateApp.setStatus(assignment.getStatus().toLowerCase());
                    }

                    templateApp.setLastChgDate(Instant.now());
                    templateApp.setLastChgBy(assignment.getLastChgBy());

                    try {
                        templateApplicationRepository.save(templateApp);
                        successfulAssignments.add(appId + " (updated)");
                    } catch (Exception e) {
                        failedAssignments.add(appId + " - Update failed: " + e.getMessage());
                    }
                } else {
                    // Create new assignment
                    TemplateApplicationRequest templateRequest = new TemplateApplicationRequest();
                    templateRequest.setTemplateId(templateId);
                    templateRequest.setAppId(appId);
                    templateRequest.setLastChgBy(assignment.getLastChgBy());
                    templateRequest.setOrderNo(assignment.getOrderNo());

                    // Set status from assignment or status update
                    String initialStatus = statusUpdates.getOrDefault(appId,
                            (assignment.getStatus() != null) ? assignment.getStatus().toLowerCase() : "n");
                    templateRequest.setStatus(initialStatus);

                    // Remove from status updates as we've applied it
                    statusUpdates.remove(appId);

                    try {
                        ApiResponse<TemplateApplicationResponse> assignmentResponse =
                                templateApplicationService.assignTemplateToApplication(templateRequest);

                        if (assignmentResponse.getStatus() == 200) {
                            successfulAssignments.add(appId);
                            newlyCreatedApps.add(appId);
                        } else {
                            failedAssignments.add(appId + " - Status: " +
                                    assignmentResponse.getStatus() + ", Message: " +
                                    assignmentResponse.getMessage());
                        }
                    } catch (Exception e) {
                        failedAssignments.add(appId + " - Error: " + e.getMessage());
                    }
                }
            }

            // Prepare template assignment messages
            if (!successfulAssignments.isEmpty()) {
                successMessages.add("Successful template assignments: " +
                        String.join(", ", successfulAssignments));
            }
            if (!skippedAssignments.isEmpty()) {
                successMessages.add("Skipped template assignments (already exists): " +
                        String.join(", ", skippedAssignments));
            }
            if (!failedAssignments.isEmpty()) {
                errors.add("Failed template assignments: " +
                        String.join(", ", failedAssignments));
            }
        }

        // Process any remaining status updates
        if (!statusUpdates.isEmpty()) {
            int updatedCount = 0;
            List<String> notFoundApps = new ArrayList<>();

            for (Map.Entry<String, String> entry : statusUpdates.entrySet()) {
                String appId = entry.getKey();
                String status = entry.getValue();

                log.info("Processing remaining status update: appId={}, status={}", appId, status);

                // Find template application entries for this app
                List<TemplateApplication> templateApps =
                        templateApplicationRepository.findByApp_AppId(appId);

                if (templateApps.isEmpty()) {
                    notFoundApps.add(appId);
                    log.warn("No template application found for appId={}", appId);
                } else {
                    // Update status in all template application entries for this app
                    for (TemplateApplication templateApp : templateApps) {
                        String oldStatus = templateApp.getStatus();
                        templateApp.setStatus(status);
                        templateApp.setLastChgDate(Instant.now());
                        templateApplicationRepository.save(templateApp);
                        updatedCount++;

                        log.info("Updated status for appId={} from {} to {}",
                                appId, oldStatus, status);
                    }
                }
            }

            if (updatedCount > 0) {
                successMessages.add("Updated status for " + updatedCount + " template applications");
            }

            if (!notFoundApps.isEmpty()) {
                successMessages.add("Skipped status updates for apps not assigned to template: " +
                        String.join(", ", notFoundApps));
            }
        }

        // Handle errors if any
        if (!errors.isEmpty()) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    String.join(". ", errors),
                    !successMessages.isEmpty() ? 207 : 400  // 207 = Partial success
            );
        }

        String finalMessage = String.join(". ", successMessages);
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
