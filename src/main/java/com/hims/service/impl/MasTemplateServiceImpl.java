package com.hims.service.impl;

import com.hims.entity.User;
import com.hims.entity.UserDepartment;
import com.hims.entity.repository.UserDepartmentRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.service.MasTemplateService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasTemplate;
import com.hims.entity.repository.MasTemplateRepository;
import com.hims.request.MasTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTemplateResponse;
import com.hims.utils.ResponseUtils;
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
public class MasTemplateServiceImpl implements MasTemplateService {

    @Autowired
    private MasTemplateRepository masTemplateRepository;

    @Autowired
    UserRepo userRepo;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;


    private static final Logger log = LoggerFactory.getLogger(DoctorRosterServicesImpl.class);

    @Override
    public ApiResponse<List<MasTemplateResponse>> getAllTemplates(int flag) {
        try {
            List<MasTemplate> templates;

            if (flag == 1) {
                templates = masTemplateRepository.findByStatusIgnoreCase("Y");
            } else if (flag == 0) {
                templates = masTemplateRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
            }

            List<MasTemplateResponse> responses = templates.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error in getAllTemplates(): ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Internal server error", 500);
        }
    }


    public ApiResponse<MasTemplateResponse> getTemplateById(Long id) {
        Optional<MasTemplate> template = masTemplateRepository.findById(id);
        return template.map(value -> ResponseUtils.createSuccessResponse(convertToResponse(value), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Template not found", 404));
    }

    public ApiResponse<MasTemplateResponse> createTemplate(MasTemplateRequest request) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }

        MasTemplate template = new MasTemplate();
        template.setTemplateCode(request.getTemplateCode());
        template.setTemplateName(request.getTemplateName());
        template.setStatus("Y"); // Default status to "Y"
        template.setLastChgBy(currentUser.getUserId());
        template.setLastChgDate(Instant.now());
        template.setHospitalId(currentUser.getHospital().getId());


        MasTemplate savedTemplate = masTemplateRepository.save(template);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedTemplate), new TypeReference<>() {});
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }



    public ApiResponse<MasTemplateResponse> updateTemplate(Long id, MasTemplateRequest request) {
        Optional<MasTemplate> existingTemplate = masTemplateRepository.findById(id);

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }

        if (existingTemplate.isPresent()) {
            MasTemplate template = existingTemplate.get();
            template.setTemplateCode(request.getTemplateCode());
            template.setTemplateName(request.getTemplateName());
            template.setLastChgBy(currentUser.getUserId());
            template.setLastChgDate(Instant.now());
            template.setHospitalId(currentUser.getHospital().getId());

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

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }

        Optional<MasTemplate> template = masTemplateRepository.findById(id);
        if (template.isPresent()) {
            MasTemplate masTemplate = template.get();
            masTemplate.setStatus(status);
            masTemplate.setLastChgDate(Instant.now());
            masTemplate.setLastChgBy(currentUser.getUserId());
            masTemplate.setHospitalId(currentUser.getHospital().getId());

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
