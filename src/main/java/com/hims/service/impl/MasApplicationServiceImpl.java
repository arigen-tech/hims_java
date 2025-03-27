package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasApplication;
import com.hims.entity.repository.MasApplicationRepository;
import com.hims.request.MasApplicationRequest;
import com.hims.request.UpdateStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasApplicationResponse;
import com.hims.service.MasApplicationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasApplicationServiceImpl implements MasApplicationService {

    @Autowired
    private MasApplicationRepository masApplicationRepository;

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
    public ApiResponse<List<MasApplicationResponse>> getAllParentApplications(int flag) {
        List<MasApplication> applications;

        if (flag == 1) {
            applications = masApplicationRepository.findByParentIdIsNullOrParentId("")
                    .stream()
                    .filter(app -> "Y".equalsIgnoreCase(app.getStatus()))
                    .collect(Collectors.toList());
        } else if (flag == 0) {
            applications = masApplicationRepository.findByParentIdIsNullOrParentId("");
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

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
