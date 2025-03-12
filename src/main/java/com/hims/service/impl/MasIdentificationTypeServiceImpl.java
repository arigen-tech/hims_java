package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasIdentificationType;
import com.hims.entity.repository.MasIdentificationTypeRepository;
import com.hims.request.MasIdentificationTypeRequest;
import com.hims.response.MasIdentificationTypeResponse;
import com.hims.response.ApiResponse;
import com.hims.service.MasIdentificationTypeService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasIdentificationTypeServiceImpl implements MasIdentificationTypeService {

    @Autowired
    private MasIdentificationTypeRepository masIdentificationTypeRepository;

    @Override
    public ApiResponse<MasIdentificationTypeResponse> addIdentificationType(MasIdentificationTypeRequest request) {
        MasIdentificationType type = new MasIdentificationType();
        type.setIdentificationCode(request.getIdentificationCode());
        type.setIdentificationName(request.getIdentificationName());
        type.setStatus(request.getStatus());
        type.setLastChangedBy(request.getLastChangedBy());
        type.setLastChangedDate(Instant.now());
        type.setMapId(request.getMapId());

        MasIdentificationType savedType = masIdentificationTypeRepository.save(type);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedType), new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<String> changeIdentificationStatus(Long id, String statusValue) {
        Optional<MasIdentificationType> typeOpt = masIdentificationTypeRepository.findById(id);
        if (typeOpt.isPresent()) {
            MasIdentificationType type = typeOpt.get();
            type.setStatus(statusValue);
            type.setLastChangedDate(Instant.now());
            masIdentificationTypeRepository.save(type);
            return ResponseUtils.createSuccessResponse("Identification type status updated", new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createNotFoundResponse("Identification type not found", 404);
        }
    }

    @Override
    public ApiResponse<MasIdentificationTypeResponse> editIdentificationType(Long id, MasIdentificationTypeRequest request) {
        Optional<MasIdentificationType> typeOpt = masIdentificationTypeRepository.findById(id);
        if (typeOpt.isPresent()) {
            MasIdentificationType type = typeOpt.get();
            type.setIdentificationCode(request.getIdentificationCode());
            type.setIdentificationName(request.getIdentificationName());
            type.setStatus(request.getStatus());
            type.setLastChangedBy(request.getLastChangedBy());
            type.setLastChangedDate(Instant.now());
            type.setMapId(request.getMapId());

            masIdentificationTypeRepository.save(type);
            return ResponseUtils.createSuccessResponse(mapToResponse(type), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createNotFoundResponse("Identification type not found", 404);
        }
    }

    @Override
    public ApiResponse<MasIdentificationTypeResponse> getIdentificationTypeById(Long id) {
        return masIdentificationTypeRepository.findById(id)
                .map(type -> ResponseUtils.createSuccessResponse(mapToResponse(type), new TypeReference<>() {
                }))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Identification type not found", 404));
    }

    @Override
    public ApiResponse<List<MasIdentificationTypeResponse>> getAllIdentificationTypes(int flag) {
        List<MasIdentificationType> types;

        if (flag == 1) {
            types = masIdentificationTypeRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            types = masIdentificationTypeRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasIdentificationTypeResponse> responses = types.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasIdentificationTypeResponse mapToResponse(MasIdentificationType type) {
        MasIdentificationTypeResponse response = new MasIdentificationTypeResponse();
        response.setIdentificationTypeId(type.getIdentificationTypeId());
        response.setIdentificationCode(type.getIdentificationCode());
        response.setIdentificationName(type.getIdentificationName());
        response.setStatus(type.getStatus());
        response.setLastChangedBy(type.getLastChangedBy());
        response.setLastChangedDate(type.getLastChangedDate());
        response.setMapId(type.getMapId());
        return response;
    }
}