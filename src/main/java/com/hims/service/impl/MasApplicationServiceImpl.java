package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasApplication;
import com.hims.entity.TemplateApplication;
import com.hims.entity.UserApplication;
import com.hims.entity.repository.MasApplicationRepository;
import com.hims.entity.repository.MasTemplateRepository;
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
    private MasTemplateRepository masTemplateRepository;

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
            applications = masApplicationRepository.findByStatusIgnoreCaseOrderByNameAsc("Y");
        } else if (flag == 0) {
            applications = masApplicationRepository.findByStatusIgnoreCaseInOrderByLastChgDateDesc(List.of("Y", "N"));
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

        // Create a list to hold all applications hierarchically
        List<MasApplicationResponse> responses = new ArrayList<>();

        // Add parent first with template status
        MasApplicationResponse parentResponse = convertToResponseWithTemplateStatus(parentApplication, templateId);

        // Recursively fetch all descendant applications (not just immediate children)
        fetchDescendants(parentId, templateId, parentResponse);

        // Add the parent with all its nested children to the response
        responses.add(parentResponse);

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    /**
     * Recursively fetches all descendants for a given parent application
     * @param parentId The parent application ID
     * @param templateId The template ID to check assignments
     * @param parentResponse The parent response object to populate with children
     */
    private void fetchDescendants(String parentId, Long templateId, MasApplicationResponse parentResponse) {
        // Fetch direct children of this parent
        List<MasApplication> childApplications = masApplicationRepository.findByParentId(parentId);

        // If no children, set children to empty list rather than null for consistency
        if (childApplications.isEmpty()) {
            parentResponse.setChildren(new ArrayList<>());
            return;
        }

        // Process each child application
        List<MasApplicationResponse> childResponses = new ArrayList<>();
        for (MasApplication child : childApplications) {
            // Convert child to response with template status
            MasApplicationResponse childResponse = convertToResponseWithTemplateStatus(child, templateId);

            // Recursively fetch its descendants
            fetchDescendants(child.getAppId(), templateId, childResponse);

            // Add to children list
            childResponses.add(childResponse);
        }

        // Set children list on parent response
        parentResponse.setChildren(childResponses);
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
        List<String> successMessages = new ArrayList<>();

        // Validate and filter valid template assignments
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

        // Validate and filter valid status updates
        List<BatchUpdateRequest.ApplicationStatusUpdate> statusUpdates = new ArrayList<>();
        if (request.getApplicationStatusUpdates() != null) {
            statusUpdates = request.getApplicationStatusUpdates().stream()
                    .filter(update ->
                            update.getTemplateId() != null &&
                                    StringUtils.hasText(update.getAppId()) &&
                                    StringUtils.hasText(update.getStatus()))
                    .peek(update -> update.setStatus(update.getStatus().toLowerCase()))
                    .collect(Collectors.toList());
        }

        if ((request.getTemplateApplicationAssignments() == null || request.getTemplateApplicationAssignments().isEmpty()) &&
                statusUpdates.isEmpty()) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "No valid updates or assignments provided.",
                    400
            );
        }

        // Process template application assignments
        Set<String> processedAppTemplateKeys = new HashSet<>();
        if (request.getTemplateApplicationAssignments() != null && !request.getTemplateApplicationAssignments().isEmpty()) {
            for (BatchUpdateRequest.TemplateApplicationAssignment assignment : request.getTemplateApplicationAssignments()) {
                try {
                    String appId = assignment.getAppId();
                    Long templateId = assignment.getTemplateId();
                    String key = appId + "_" + templateId;

                    Optional<TemplateApplication> optionalTemplateApp =
                            templateApplicationRepository.findByTemplate_IdAndApp_AppId(templateId, appId);

                    TemplateApplication templateApp;
                    if (optionalTemplateApp.isPresent()) {
                        templateApp = optionalTemplateApp.get();
                        templateApp.setStatus(assignment.getStatus().toLowerCase());
                        templateApp.setOrderNo(assignment.getOrderNo());
                        templateApp.setLastChgBy(assignment.getLastChgBy());
                        templateApp.setLastChgDate(Instant.now());
                        log.info("Updated template assignment for appId={}, templateId={}", appId, templateId);
                    } else {
                        // Create new assignment
                        templateApp = new TemplateApplication();
                        templateApp.setApp(masApplicationRepository.findById(appId).orElseThrow(() ->
                                new IllegalArgumentException("App not found: " + appId)));
                        templateApp.setTemplate(masTemplateRepository.findById(templateId).orElseThrow(() ->
                                new IllegalArgumentException("Template not found: " + templateId)));
                        templateApp.setOrderNo(assignment.getOrderNo());
                        templateApp.setStatus(assignment.getStatus().toLowerCase());
                        templateApp.setLastChgBy(assignment.getLastChgBy());
                        templateApp.setLastChgDate(Instant.now());
                        log.info("Created new template assignment for appId={}, templateId={}", appId, templateId);
                    }

                    templateApplicationRepository.save(templateApp);
                    successMessages.add("Processed assignment for appId=" + appId + ", templateId=" + templateId);
                    processedAppTemplateKeys.add(key);

                } catch (Exception ex) {
                    log.error("Error processing template assignment: {}", ex.getMessage(), ex);
                    errors.add("Failed to process assignment for appId=" + assignment.getAppId());
                }
            }
        }

        // Process status updates (only those not already handled above)
        int updatedCount = 0;
        List<String> notFoundApps = new ArrayList<>();

        for (BatchUpdateRequest.ApplicationStatusUpdate update : statusUpdates) {
            String appId = update.getAppId();
            Long templateId = update.getTemplateId();
            String status = update.getStatus();
            String key = appId + "_" + templateId;

            if (processedAppTemplateKeys.contains(key)) continue;

            Optional<TemplateApplication> optionalTemplateApp =
                    templateApplicationRepository.findByTemplate_IdAndApp_AppId(templateId, appId);

            if (optionalTemplateApp.isPresent()) {
                TemplateApplication templateApp = optionalTemplateApp.get();
                String oldStatus = templateApp.getStatus();
                templateApp.setStatus(status);
                templateApp.setLastChgDate(Instant.now());
                templateApplicationRepository.save(templateApp);
                updatedCount++;
                log.info("Updated status for appId={}, templateId={} from {} to {}", appId, templateId, oldStatus, status);
            } else {
                notFoundApps.add("appId=" + appId + ", templateId=" + templateId);
                log.warn("No template application found for appId={}, templateId={}", appId, templateId);
            }
        }

        if (updatedCount > 0) {
            successMessages.add("Updated status for " + updatedCount + " template applications");
        }
        if (!notFoundApps.isEmpty()) {
            successMessages.add("Skipped status updates for unassigned template applications: " + String.join(", ", notFoundApps));
        }

        // Return response
        if (!errors.isEmpty()) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    String.join(". ", errors),
                    !successMessages.isEmpty() ? 207 : 400  // 207 = Multi-Status (Partial Success)
            );
        }

        String finalMessage = String.join(". ", successMessages);
        return ResponseUtils.createSuccessResponse(
                finalMessage.isEmpty() ? "No updates processed." : finalMessage,
                new TypeReference<>() {}
        );
    }




    @Override
    public ApiResponse<List<MasApplicationResponse>> getAllParentApplications(int flag) {
        List<MasApplication> applications;

        // First get applications based on status flag
        if (flag == 1) {
            applications = masApplicationRepository.findByStatusIgnoreCaseOrderByNameAsc("Y");
        } else if (flag == 0) {
            applications = masApplicationRepository.findAllByOrderByLastChgDateDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }


        // Filter to get parent applications (where parentId is "0" OR url is "#")
        applications = applications.stream()
                .filter(app -> (app.getParentId() != null && app.getParentId().equals("0"))
                        || (app.getUrl() != null && app.getUrl().equals("#")))
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
