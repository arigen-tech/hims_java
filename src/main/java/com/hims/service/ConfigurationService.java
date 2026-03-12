package com.hims.service;

import com.hims.request.TemplateApplicationRequest;
import com.hims.request.UserApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.TemplateApplicationResponse;
import com.hims.response.UrlByRoleResponse;
import com.hims.response.UserApplicationResponse;

import java.util.List;

/**
 * Unified Configuration Service
 * Handles all system configuration operations:
 * - User Applications
 * - Template Applications
 * - URL/Role Mappings
 */
public interface ConfigurationService {

    // ==================== USER APPLICATION OPERATIONS ====================

    /**
     * Get all user applications
     * @param flag 0=all, 1=active
     */
    ApiResponse<List<UserApplicationResponse>> getAllApplications(int flag);

    /**
     * Get user application by ID
     */
    ApiResponse<UserApplicationResponse> getApplicationById(Long id);

    /**
     * Create new user application
     */
    ApiResponse<UserApplicationResponse> createApplication(UserApplicationRequest request);

    /**
     * Update existing user application
     */
    ApiResponse<UserApplicationResponse> updateApplication(Long id, UserApplicationRequest request);

    /**
     * Change user application status
     */
    ApiResponse<String> changeApplicationStatus(Long id, String status);

    /**
     * Get all parent applications with hash URL
     * @param flag 0=all, 1=active
     */
    ApiResponse<List<UserApplicationResponse>> getParentApplications(int flag);

    // ==================== TEMPLATE APPLICATION OPERATIONS ====================

    /**
     * Assign template to application
     */
    ApiResponse<TemplateApplicationResponse> assignTemplateToApplication(TemplateApplicationRequest request);

    /**
     * Change template application status
     */
    ApiResponse<String> changeTemplateApplicationStatus(Long id, String status);

    /**
     * Get all template applications by template ID
     */
    ApiResponse<List<TemplateApplicationResponse>> getTemplateApplicationsByTemplateId(Long templateId);

    /**
     * Get all template applications
     * @param flag 0=all, 1=active
     */
    ApiResponse<List<TemplateApplicationResponse>> getAllTemplateApplications(int flag);

    // ==================== URL BY ROLE OPERATIONS ====================

    /**
     * Get all URLs accessible by multiple roles
     * @param roleIds List of role IDs
     */
    ApiResponse<List<UrlByRoleResponse>> getUrlsByRoles(List<Long> roleIds);
}


