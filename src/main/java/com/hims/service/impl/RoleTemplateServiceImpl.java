package com.hims.service.impl;

import com.hims.entity.MasTemplate;
import com.hims.entity.RoleTemplate;
import com.hims.entity.repository.MasTemplateRepository;
import com.hims.entity.repository.RoleTemplateRepository;
import com.hims.request.RoleTemplateRequestList;
import com.hims.response.ApiResponse;
import com.hims.response.RoleTemplateResponse;
import com.hims.service.RoleTemplateService;
import com.hims.utils.ResponseUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleTemplateServiceImpl implements RoleTemplateService {

    @Autowired
    private MasTemplateRepository masTemplateRepository;

    @Autowired
    private RoleTemplateRepository roleTemplateRepository;

    @Override
    public ApiResponse<List<RoleTemplateResponse>> addOrUpdateRoleTemplates(RoleTemplateRequestList requestList) {
        List<RoleTemplateResponse> responses = requestList.getApplicationStatusUpdates().stream().map(request -> {
            // Retrieve or create MasTemplate
            MasTemplate template = masTemplateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new RuntimeException("Template not found with ID: " + request.getTemplateId()));

            // Check if role-template mapping exists
            RoleTemplate roleTemplate = roleTemplateRepository.findByRoleIdAndTemplateId(
                    request.getRoleId(), request.getTemplateId()).orElse(null);

            if (roleTemplate == null) {
                // Create new RoleTemplate if it doesn't exist
                roleTemplate = new RoleTemplate();
                roleTemplate.setRoleId(request.getRoleId());
                roleTemplate.setTemplate(template);  // Set MasTemplate object
                roleTemplate.setStatus(request.getStatus());
                roleTemplate.setLastChgBy(request.getLastChgBy());
                roleTemplate.setLastChgDate(Instant.now());
            } else {
                // Update existing RoleTemplate
                roleTemplate.setStatus(request.getStatus());
                roleTemplate.setLastChgBy(request.getLastChgBy());
                roleTemplate.setLastChgDate(Instant.now());
            }

            RoleTemplate savedRoleTemplate = roleTemplateRepository.save(roleTemplate);
            return convertToResponse(savedRoleTemplate);
        }).collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<RoleTemplateResponse>> getTemplatesByRoleId(Long roleId, int flag) {
        List<RoleTemplate> templates = roleTemplateRepository.findByRoleId(roleId);

        if (flag == 1) {
            templates = templates.stream()
                    .filter(template -> "y".equals(template.getStatus()))
                    .collect(Collectors.toList());
        }

        List<RoleTemplateResponse> responses = templates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private RoleTemplateResponse convertToResponse(RoleTemplate roleTemplate) {
        RoleTemplateResponse response = new RoleTemplateResponse();
        response.setId(roleTemplate.getId());
        response.setRoleId(roleTemplate.getRoleId());
        response.setTemplateId(roleTemplate.getTemplate().getId()); // Get template ID from MasTemplate entity
        response.setStatus(roleTemplate.getStatus());
        response.setLastChgBy(roleTemplate.getLastChgBy());
        return response;
    }

}
