package com.hims.controller;

import com.hims.request.TemplateApplicationRequest;
import com.hims.request.UserApplicationRequest;
import com.hims.response.*;
import com.hims.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration Controller
 * Manages system configuration including applications, templates, and role-based URLs
 */
@RestController
@RequestMapping("/configuration")
@Slf4j
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    // ==================== USER APPLICATION OPERATIONS ====================

    /**
     * Get all user applications by flag
     * @param flag 0=all, 1=active
     */
    @GetMapping("/getApplications")
    public ApiResponse<List<UserApplicationResponse>> getAllApplications(@RequestParam int flag) {
        log.info("Received request to get all applications with flag: {}", flag);
        return configurationService.getAllApplications(flag);
    }

    /**
     * Get user application by ID
     */
    @GetMapping("/getApplication/{id}")
    public ResponseEntity<ApiResponse<UserApplicationResponse>> getApplicationById(@PathVariable Long id) {
        log.info("Received request to get application by id: {}", id);
        return new ResponseEntity<>(configurationService.getApplicationById(id), HttpStatus.OK);
    }

    /**
     * Create new user application
     */
    @PostMapping("/createApplication")
    public ResponseEntity<ApiResponse<UserApplicationResponse>> createApplication(
            @RequestBody UserApplicationRequest request) {
        log.info("Received request to create application: {}", request);
        return new ResponseEntity<>(configurationService.createApplication(request), HttpStatus.CREATED);
    }

    /**
     * Update existing user application
     */
    @PutMapping("/updateApplication/{id}")
    public ResponseEntity<ApiResponse<UserApplicationResponse>> updateApplication(
            @PathVariable Long id,
            @RequestBody UserApplicationRequest request) {
        log.info("Received request to update application with id: {}, request: {}", id, request);
        return new ResponseEntity<>(configurationService.updateApplication(id, request), HttpStatus.OK);
    }

    /**
     * Change user application status
     */
    @PutMapping("/changeApplicationStatus/{id}")
    public ResponseEntity<ApiResponse<String>> changeApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Received request to change application status for id: {}, status: {}", id, status);
        return new ResponseEntity<>(configurationService.changeApplicationStatus(id, status), HttpStatus.OK);
    }

    /**
     * Get all parent applications with hash URL
     * @param flag 0=all, 1=active
     */
    @GetMapping("/getParentApplications")
    public ApiResponse<List<UserApplicationResponse>> getParentApplications(@RequestParam int flag) {
        log.info("Received request to get parent applications with flag: {}", flag);
        return configurationService.getParentApplications(flag);
    }

    // ==================== TEMPLATE APPLICATION OPERATIONS ====================

    /**
     * Assign template to application
     */
    @PostMapping("/assignTemplateToApplication")
    public ResponseEntity<ApiResponse<TemplateApplicationResponse>> assignTemplateToApplication(
            @RequestBody TemplateApplicationRequest request) {
        log.info("Received request to assign template to application: {}", request);
        ApiResponse<TemplateApplicationResponse> response =
                configurationService.assignTemplateToApplication(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Change template application status
     */
    @PutMapping("/changeTemplateStatus/{id}")
    public ResponseEntity<ApiResponse<String>> changeTemplateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Received request to change template application status for id: {}, status: {}", id, status);
        ApiResponse<String> response =
                configurationService.changeTemplateApplicationStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all template applications by template ID
     */
    @GetMapping("/getTemplateApplicationsByTemplate/{templateId}")
    public ResponseEntity<ApiResponse<List<TemplateApplicationResponse>>> getTemplateApplicationsByTemplateId(
            @PathVariable Long templateId) {
        log.info("Received request to get template applications by templateId: {}", templateId);
        return ResponseEntity.ok(configurationService.getTemplateApplicationsByTemplateId(templateId));
    }

    /**
     * Get all template applications by flag
     * @param flag 0=all, 1=active
     */
    @GetMapping("/getTemplateApplications")
    public ApiResponse<List<TemplateApplicationResponse>> getAllTemplateApplications(@RequestParam int flag) {
        log.info("Received request to get all template applications with flag: {}", flag);
        return configurationService.getAllTemplateApplications(flag);
    }

    // ==================== URL BY ROLE OPERATIONS ====================

    /**
     * Get all URLs accessible by multiple roles
     * @param roleIds Comma-separated role IDs (e.g., "1,2,3")
     */
    @GetMapping("/getUrlsByRoles/{roleIds}")
    public ApiResponse<List<UrlByRoleResponse>> getUrlsByRoles(@PathVariable String roleIds) {
        log.info("Received request to get URLs by roles: {}", roleIds);
        List<Long> roleIdList = Arrays.stream(roleIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return configurationService.getUrlsByRoles(roleIdList);
    }

    /**
     * Fetches the parent application along with all its descendant applications
     * in hierarchical structure and also marks whether each application is
     * assigned to the given templateId or not.
     *
     * @param parentId the root parent application id
     * @param templateId optional template id to check assignment status
     * @return hierarchical list of applications with assigned/status details
     */
    @GetMapping("/getAllChildrenByParentId/{parentId}")
    public ResponseEntity<ApiResponse<List<MasApplicationResponse>>> getAllByParentId(@PathVariable String parentId, @RequestParam(required = false) Long templateId) {
        log.info("Received request to fetch all children by parentId: {}, templateId: {}", parentId, templateId);
        return new ResponseEntity<>(configurationService.getAllByParentId(parentId, templateId), HttpStatus.OK);
    }

}







