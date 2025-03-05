package com.hims.service.impl;

import com.hims.service.MasTemplateService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasTemplate;
import com.hims.entity.repository.MasTemplateRepository;
import com.hims.request.MasTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTemplateResponse;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasTemplateServiceImpl implements MasTemplateService {

    @Autowired
    private MasTemplateRepository masTemplateRepository;

    public ApiResponse<List<MasTemplateResponse>> getAllTemplates() {
        List<MasTemplate> templates = masTemplateRepository.findAll();
        List<MasTemplateResponse> responses = templates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    public ApiResponse<MasTemplateResponse> getTemplateById(Long id) {
        Optional<MasTemplate> template = masTemplateRepository.findById(id);
        return template.map(value -> ResponseUtils.createSuccessResponse(convertToResponse(value), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Template not found", 404));
    }

    public ApiResponse<MasTemplateResponse> createTemplate(MasTemplateRequest request) {
        MasTemplate template = new MasTemplate();
        template.setTemplateCode(request.getTemplateCode());
        template.setTemplateName(request.getTemplateName());
        template.setStatus("Y"); // Default status to "Y"
        template.setLastChgBy(request.getLastChgBy());
        template.setLastChgDate(Instant.now());
        template.setHospitalId(request.getHospitalId());

        MasTemplate savedTemplate = masTemplateRepository.save(template);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedTemplate), new TypeReference<>() {});
    }

    public ApiResponse<MasTemplateResponse> updateTemplate(Long id, MasTemplateRequest request) {
        Optional<MasTemplate> existingTemplate = masTemplateRepository.findById(id);
        if (existingTemplate.isPresent()) {
            MasTemplate template = existingTemplate.get();
            template.setTemplateCode(request.getTemplateCode());
            template.setTemplateName(request.getTemplateName());
            template.setLastChgBy(request.getLastChgBy());
            template.setLastChgDate(Instant.now());
            template.setHospitalId(request.getHospitalId());

            MasTemplate updatedTemplate = masTemplateRepository.save(template);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedTemplate), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Template not found", 404);
        }
    }

    public ApiResponse<String> changeTemplateStatus(Long id, String status) {
        if (!isValidStatus(status)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        Optional<MasTemplate> template = masTemplateRepository.findById(id);
        if (template.isPresent()) {
            MasTemplate masTemplate = template.get();
            masTemplate.setStatus(status);
            masTemplate.setLastChgDate(Instant.now());
            masTemplateRepository.save(masTemplate);
            return ResponseUtils.createSuccessResponse("Template status updated to '" + status + "'", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Template not found", 404);
        }
    }

    private boolean isValidStatus(String status) {
        return "Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status);
    }

    private MasTemplateResponse convertToResponse(MasTemplate template) {
        MasTemplateResponse response = new MasTemplateResponse();
        response.setId(template.getId());
        response.setTemplateCode(template.getTemplateCode());
        response.setTemplateName(template.getTemplateName());
        response.setStatus(template.getStatus());
        response.setLastChgBy(template.getLastChgBy());
        response.setLastChgDate(template.getLastChgDate());
        response.setHospitalId(template.getHospitalId());
        return response;
    }
}
