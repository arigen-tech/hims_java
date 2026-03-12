package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.TemplateApplicationProjection;
import com.hims.projection.UrlAppProjection;
import com.hims.request.TemplateApplicationRequest;
import com.hims.request.UserApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.TemplateApplicationResponse;
import com.hims.response.UrlByRoleResponse;
import com.hims.response.UserApplicationResponse;
import com.hims.service.ConfigurationService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified Configuration Service Implementation
 * Contains all business logic from UserApplicationService, TemplateApplicationService, and UrlByRoleService
 * Eliminates the need for 3 separate service implementations
 */
@Service
@Slf4j
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    private UserApplicationRepository userApplicationRepository;

    @Autowired
    private TemplateApplicationRepository templateApplicationRepository;

    @Autowired
    private MasTemplateRepository masTemplateRepository;

    @Autowired
    private MasApplicationRepository masApplicationRepository;

    @Autowired
    private RoleTemplateRepository roleTemplateRepository;

    @Autowired
    private UserRepo userRepo;

    // ==================== USER APPLICATION OPERATIONS ====================

    @Override
    public ApiResponse<List<UserApplicationResponse>> getAllApplications(int flag) {
        try {
            List<UserApplication> applications;

            if (flag == 1) {
                applications = userApplicationRepository.findByStatusIgnoreCase(AppConstants.STATUS_Y);
            } else if (flag == 0) {
                applications = userApplicationRepository.findByStatusInIgnoreCase(List.of(AppConstants.STATUS_Y, AppConstants.STATUS_N));
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value. Use 0 for all or 1 for active.", 400);
            }

            List<UserApplicationResponse> responses = applications.stream()
                    .map(this::convertUserAppToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching user applications: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error fetching applications: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<UserApplicationResponse> getApplicationById(Long id) {
        try {
            Optional<UserApplication> application = userApplicationRepository.findById(id);
            return application.map(value -> ResponseUtils.createSuccessResponse(
                            convertUserAppToResponse(value), new TypeReference<>() {}))
                    .orElseGet(() -> ResponseUtils.createNotFoundResponse("Application not found", 404));
        } catch (Exception e) {
            log.error("Error fetching application by ID: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error fetching application: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<UserApplicationResponse> createApplication(UserApplicationRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            UserApplication application = new UserApplication();
            application.setUserAppName(request.getUserAppName());
            application.setUrl(request.getUrl());
            application.setStatus(AppConstants.STATUS_Y.toLowerCase());
            application.setLastChgBy(currentUser.getUserId());
            application.setLastChgDate(Instant.now());

            UserApplication savedApplication = userApplicationRepository.save(application);
            return ResponseUtils.createSuccessResponse(
                    convertUserAppToResponse(savedApplication), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating application: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error creating application: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<UserApplicationResponse> updateApplication(Long id, UserApplicationRequest request) {
        try {
            Optional<UserApplication> existingApplication = userApplicationRepository.findById(id);

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            if (existingApplication.isPresent()) {
                UserApplication application = existingApplication.get();
                application.setUserAppName(request.getUserAppName());
                application.setUrl(request.getUrl());
                application.setLastChgBy(currentUser.getUserId());
                application.setLastChgDate(Instant.now());

                UserApplication updatedApplication = userApplicationRepository.save(application);
                return ResponseUtils.createSuccessResponse(
                        convertUserAppToResponse(updatedApplication), new TypeReference<>() {});
            } else {
                return ResponseUtils.createNotFoundResponse("Application not found", 404);
            }
        } catch (Exception e) {
            log.error("Error updating application: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error updating application: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> changeApplicationStatus(Long id, String status) {
        try {
            if (!isValidStatus(status)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status. Status should be 'Y' or 'N'", 400);
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            Optional<UserApplication> application = userApplicationRepository.findById(id);
            if (application.isPresent()) {
                UserApplication userApplication = application.get();
                userApplication.setStatus(status);
                userApplication.setLastChgDate(Instant.now());
                userApplication.setLastChgBy(currentUser.getUserId());
                userApplicationRepository.save(userApplication);
                return ResponseUtils.createSuccessResponse(
                        "Application status updated to '" + status + "'", new TypeReference<>() {});
            } else {
                return ResponseUtils.createNotFoundResponse("Application not found", 404);
            }
        } catch (Exception e) {
            log.error("Error changing application status: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error changing status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<UserApplicationResponse>> getParentApplications(int flag) {
        try {
            List<UserApplication> applications;

            if (flag == 1) {
                applications = userApplicationRepository.findByStatusIgnoreCaseAndUrl(AppConstants.STATUS_Y, "#");
            } else if (flag == 0) {
                applications = userApplicationRepository.findByStatusInIgnoreCaseAndUrl(List.of(AppConstants.STATUS_Y, AppConstants.STATUS_N), "#");
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value. Use 0 for all or 1 for active.", 400);
            }

            List<UserApplicationResponse> responses = applications.stream()
                    .map(this::convertUserAppToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching parent applications: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error fetching parent applications: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // ==================== TEMPLATE APPLICATION OPERATIONS ====================

    @Override
    public ApiResponse<TemplateApplicationResponse> assignTemplateToApplication(TemplateApplicationRequest request) {
        try {
            Optional<MasTemplate> templateOpt = masTemplateRepository.findById(request.getTemplateId());
            Optional<MasApplication> appOpt = masApplicationRepository.findById(request.getAppId());

            if (templateOpt.isEmpty() || appOpt.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Template or Application not found", 404);
            }

            TemplateApplication templateApplication = new TemplateApplication();
            templateApplication.setTemplate(templateOpt.get());
            templateApplication.setApp(appOpt.get());
            templateApplication.setStatus(AppConstants.STATUS_Y.toLowerCase());
            templateApplication.setLastChgDate(Instant.now());
            templateApplication.setLastChgBy(request.getLastChgBy());
            templateApplication.setOrderNo(request.getOrderNo());

            templateApplication = templateApplicationRepository.save(templateApplication);
            return ResponseUtils.createSuccessResponse(
                    convertTemplateAppToResponse(templateApplication), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error assigning template to application: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error assigning template: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> changeTemplateApplicationStatus(Long id, String status) {
        try {
            if (!isValidStatus(status)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status. Status should be 'Y' or 'N'", 400);
            }

            Optional<TemplateApplication> templateApplicationOpt = templateApplicationRepository.findById(id);
            if (templateApplicationOpt.isPresent()) {
                TemplateApplication templateApplication = templateApplicationOpt.get();
                templateApplication.setStatus(status);
                templateApplication.setLastChgDate(Instant.now());
                templateApplicationRepository.save(templateApplication);
                return ResponseUtils.createSuccessResponse(
                        "Template Application status updated to '" + status + "'", new TypeReference<>() {});
            } else {
                return ResponseUtils.createNotFoundResponse("Template Application not found", 404);
            }
        } catch (Exception e) {
            log.error("Error changing template application status: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error changing status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<TemplateApplicationResponse>> getTemplateApplicationsByTemplateId(Long templateId) {
        try {
            Optional<MasTemplate> templateOpt = masTemplateRepository.findById(templateId);

            if (templateOpt.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Template not found",
                        404
                );
            }

            List<TemplateApplicationProjection> projections = templateApplicationRepository.getTemplateApplicationsByTemplateId(templateId);

            List<TemplateApplicationResponse> responses = projections.stream()
                    .map(this::convertProjectionToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching template applications: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error fetching template applications: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<TemplateApplicationResponse>> getAllTemplateApplications(int flag) {
        try {
            List<TemplateApplicationProjection> projections;

            if (flag == 1) {
                projections = templateApplicationRepository
                        .getAllTemplateApplicationsByStatus(AppConstants.STATUS_Y.toLowerCase());
            } else if (flag == 0) {
                projections = templateApplicationRepository
                        .getAllTemplateApplicationsByStatuses(
                                List.of(AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase())
                        );
            } else {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid flag value. Use 0 for all or 1 for active.",
                        400
                );
            }

            List<TemplateApplicationResponse> responses = projections.stream()
                    .map(this::convertProjectionToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching template applications: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error fetching template applications: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    // ==================== URL BY ROLE OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<UrlByRoleResponse>> getUrlsByRoles(List<Long> roleIds) {
        final String NOT_FOUND_MESSAGE = "No application menu found for given roles";

        try {
            log.info("Fetching application menu for roleIds: {}", roleIds);

            List<Long> templateIdList = roleTemplateRepository.findActiveTemplateIdsByRoleIds(roleIds, AppConstants.STATUS_Y);

            if (templateIdList == null || templateIdList.isEmpty()) {
                log.warn("No active role-template mapping found for roleIds: {}", roleIds);
                return ResponseUtils.createNotFoundResponse(NOT_FOUND_MESSAGE, 404);
            }

            Set<Long> templateIds = new HashSet<>(templateIdList);

            if (templateIds.isEmpty()) {
                log.warn("No template IDs found for roleIds: {}", roleIds);
                return ResponseUtils.createNotFoundResponse(NOT_FOUND_MESSAGE, 404);
            }

            List<UrlAppProjection> applicationList = templateApplicationRepository.findActiveAppDetailsByTemplateIds(templateIds,
                            AppConstants.STATUS_Y
                    );

            if (applicationList == null || applicationList.isEmpty()) {
                log.warn("No active applications found for templateIds: {}", templateIds);
                return ResponseUtils.createNotFoundResponse(NOT_FOUND_MESSAGE, 404);
            }

            Map<String, UrlAppProjection> accessibleApps = new LinkedHashMap<>();
            for (UrlAppProjection app : applicationList) {
                if (app.getAppId() != null) {
                    accessibleApps.putIfAbsent(app.getAppId(), app);
                }
            }

            if (accessibleApps.isEmpty()) {
                log.warn("No unique active applications found for templateIds: {}", templateIds);
                return ResponseUtils.createNotFoundResponse(NOT_FOUND_MESSAGE, 404);
            }

            Map<String, List<UrlAppProjection>> childrenByParentId =
                    new HashMap<>(accessibleApps.size());

            for (UrlAppProjection app : accessibleApps.values()) {
                String parentId = app.getParentId();
                if (parentId != null) {
                    childrenByParentId
                            .computeIfAbsent(parentId, k -> new ArrayList<>())
                            .add(app);
                }
            }

            Comparator<UrlAppProjection> appOrderComparator = Comparator.comparing(
                    app -> app.getOrderNo() != null ? app.getOrderNo() : Long.MAX_VALUE
            );

            for (List<UrlAppProjection> childList : childrenByParentId.values()) {
                childList.sort(appOrderComparator);
            }

            List<UrlAppProjection> rootApps =
                    childrenByParentId.getOrDefault("0", Collections.emptyList());

            if (rootApps.isEmpty()) {
                log.warn("No root applications found for roleIds: {}", roleIds);
                return ResponseUtils.createNotFoundResponse(NOT_FOUND_MESSAGE, 404);
            }

            List<UrlByRoleResponse> result = new ArrayList<>(rootApps.size());

            for (UrlAppProjection rootApp : rootApps) {
                UrlByRoleResponse rootNode = convertProjectionToUrlResponse(rootApp);
                buildHierarchy(rootNode, rootApp.getAppId(), childrenByParentId);
                result.add(rootNode);
            }

            log.info("Successfully fetched {} root application(s) for roleIds: {}", result.size(), roleIds);

            return ResponseUtils.createSuccessResponse(
                    result,
                    new TypeReference<List<UrlByRoleResponse>>() {}
            );

        } catch (Exception e) {
            log.error("Error while fetching application menu for roleIds: {}", roleIds, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<UrlByRoleResponse>>() {},
                    "Something went wrong while fetching application menu",
                    500
            );
        }


    }

    // ==================== HELPER METHODS ====================

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private boolean isValidStatus(String status) {
        return "y".equalsIgnoreCase(status) || "n".equalsIgnoreCase(status);
    }

    private UserApplicationResponse convertUserAppToResponse(UserApplication application) {
        UserApplicationResponse response = new UserApplicationResponse();
        response.setId(application.getId());
        response.setUserAppName(application.getUserAppName());
        response.setUrl(application.getUrl());
        response.setStatus(application.getStatus());
        response.setLastChgBy(application.getLastChgBy());
        response.setLastChgDate(application.getLastChgDate());
        return response;
    }

    private TemplateApplicationResponse convertTemplateAppToResponse(TemplateApplication application) {
        TemplateApplicationResponse response = new TemplateApplicationResponse();

        if (application.getTemplate() != null) {
            response.setTemplateId(application.getTemplate().getId());
        }

        if (application.getApp() != null) {
            response.setAppId(application.getApp().getAppId());
            response.setAppName(application.getApp().getName());
            response.setParentId(application.getApp().getParentId());
        }

        response.setStatus(application.getStatus());
        response.setLastChgDate(application.getLastChgDate());

        return response;
    }

    private UrlByRoleResponse convertMasAppToUrlResponse(MasApplication app) {
        UrlByRoleResponse response = new UrlByRoleResponse();
        response.setAppId(app.getAppId());
        response.setName(app.getName());
        response.setUrl(app.getUrl());
        response.setChildren(new ArrayList<>());
        return response;
    }

//    private void buildHierarchy(UrlByRoleResponse parent, String parentId,
//                                Map<String, List<MasApplication>> childrenByParentId) {
//        List<MasApplication> children = childrenByParentId.getOrDefault(parentId, Collections.emptyList());
//
//        children.sort(Comparator.comparing(app -> app.getOrderNo() != null ? app.getOrderNo() : Long.MAX_VALUE));
//
//        for (MasApplication childApp : children) {
//            UrlByRoleResponse childNode = convertMasAppToUrlResponse(childApp);
//            parent.getChildren().add(childNode);
//            buildHierarchy(childNode, childApp.getAppId(), childrenByParentId);
//        }
//    }
    private TemplateApplicationResponse convertProjectionToResponse(TemplateApplicationProjection projection) {
        TemplateApplicationResponse response = new TemplateApplicationResponse();

        response.setId(projection.getId());
        response.setTemplateId(projection.getTemplateId());
        response.setAppId(projection.getAppId());
        response.setAppName(projection.getAppName());
        response.setStatus(projection.getStatus());
        response.setLastChgDate(projection.getLastChgDate());
        response.setLastChgBy(projection.getLastChgBy());
        response.setOrderNo(projection.getOrderNo());
        response.setParentId(projection.getParentId());

        return response;
    }

    private UrlByRoleResponse convertProjectionToUrlResponse(UrlAppProjection app) {
        UrlByRoleResponse response = new UrlByRoleResponse();
        response.setAppId(app.getAppId());
        response.setName(app.getName());
        response.setUrl(app.getUrl());
        response.setChildren(new ArrayList<>());
        return response;
    }

    private void buildHierarchy(UrlByRoleResponse parentNode,
                                String parentAppId,
                                Map<String, List<UrlAppProjection>> childrenByParentId) {

        List<UrlAppProjection> childrenApps = childrenByParentId.get(parentAppId);

        if (childrenApps == null || childrenApps.isEmpty()) {
            parentNode.setChildren(Collections.emptyList());
            return;
        }

        List<UrlByRoleResponse> childResponses = new ArrayList<>(childrenApps.size());

        for (UrlAppProjection childApp : childrenApps) {
            UrlByRoleResponse childNode = convertProjectionToUrlResponse(childApp);
            buildHierarchy(childNode, childApp.getAppId(), childrenByParentId);
            childResponses.add(childNode);
        }

        parentNode.setChildren(childResponses);
    }
}


