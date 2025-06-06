package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasApplication;
import com.hims.entity.MasTemplate;
import com.hims.entity.TemplateApplication;
import com.hims.entity.repository.MasApplicationRepository;
import com.hims.entity.repository.MasTemplateRepository;
import com.hims.entity.repository.TemplateApplicationRepository;
import com.hims.request.TemplateApplicationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.TemplateApplicationResponse;
import com.hims.service.TemplateApplicationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TemplateApplicationServiceImpl implements TemplateApplicationService {

    @Autowired
    private TemplateApplicationRepository templateApplicationRepository;

    @Autowired
    private MasTemplateRepository masTemplateRepository;

    @Autowired
    private MasApplicationRepository masApplicationRepository;

    @Override
    public ApiResponse<TemplateApplicationResponse> assignTemplateToApplication(TemplateApplicationRequest request) {
        Optional<MasTemplate> templateOpt = masTemplateRepository.findById(request.getTemplateId());
        Optional<MasApplication> appOpt = masApplicationRepository.findById(request.getAppId());

        if (templateOpt.isEmpty() || appOpt.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Template or Application not found", 404);
        }

        TemplateApplication templateApplication = new TemplateApplication();
        templateApplication.setTemplate(templateOpt.get());
        templateApplication.setApp(appOpt.get());
        templateApplication.setStatus("y");
        templateApplication.setLastChgDate(Instant.now());
        templateApplication.setLastChgBy(request.getLastChgBy());
        templateApplication.setOrderNo(request.getOrderNo());

        templateApplication = templateApplicationRepository.save(templateApplication);
        return ResponseUtils.createSuccessResponse(convertToResponse(templateApplication), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeTemplateApplicationStatus(Long id, String status) {
        if (!isValidStatus(status)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        Optional<TemplateApplication> templateApplicationOpt = templateApplicationRepository.findById(id);
        if (templateApplicationOpt.isPresent()) {
            TemplateApplication templateApplication = templateApplicationOpt.get();
            templateApplication.setStatus(status);
            templateApplication.setLastChgDate(Instant.now());
            templateApplicationRepository.save(templateApplication);
            return ResponseUtils.createSuccessResponse("Template Application status updated to '" + status + "'", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Template Application not found", 404);
        }
    }

    @Override
    public ApiResponse<List<TemplateApplicationResponse>> getAllTemplateApplications(int flag) {
        List<TemplateApplication> templateApplications;

        if (flag == 1) {
            templateApplications = templateApplicationRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            templateApplications = templateApplicationRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<TemplateApplicationResponse> responses = templateApplications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<TemplateApplicationResponse>> getAllTemplateById(Long templateId) {
        Optional<MasTemplate> templateOpt = masTemplateRepository.findById(templateId);

        if (templateOpt.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Template not found", 404);
        }

        List<TemplateApplication> templateApplications = templateApplicationRepository.findByTemplateId(templateId);

        List<TemplateApplicationResponse> responses = templateApplications.stream()
                .map(this::convertToResponse)
                .filter(app -> app.getAppId() != null && "0".equals(app.getParentId()))  // Filter parent applications
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private TemplateApplicationResponse convertToResponse(TemplateApplication application) {
        TemplateApplicationResponse response = new TemplateApplicationResponse();

        // Set fields from the relationship entities
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

    private boolean isValidStatus(String status) {
        return "Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status);
    }

}
