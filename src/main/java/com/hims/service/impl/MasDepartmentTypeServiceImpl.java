package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartmentType;
import com.hims.entity.repository.MasDepartmentTypeRepository;
import com.hims.request.MasDepartmentTypeRequest;
import com.hims.response.MasDepartmentTypeResponse;
import com.hims.response.ApiResponse;
import com.hims.service.MasDepartmentTypeService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasDepartmentTypeServiceImpl implements MasDepartmentTypeService {

    @Autowired
    private MasDepartmentTypeRepository masDepartmentTypeRepository;

    private boolean isValidStatus(String status) {
        return "Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status);
    }

    @Override
    public ApiResponse<MasDepartmentTypeResponse> addDepartmentType(MasDepartmentTypeRequest request) {
        if (!isValidStatus(request.getStatus())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        MasDepartmentType departmentType = new MasDepartmentType();
        departmentType.setDepartmentTypeCode(request.getDepartmentTypeCode());
        departmentType.setDepartmentTypeName(request.getDepartmentTypeName());
        departmentType.setStatus(request.getStatus());
        departmentType.setLastChgBy(request.getLastChgBy());
        departmentType.setLastChgDate(Instant.now());

        MasDepartmentType savedDepartmentType = masDepartmentTypeRepository.save(departmentType);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedDepartmentType), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeDepartmentTypeStatus(Long id, String status) {
        if (!isValidStatus(status)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        Optional<MasDepartmentType> departmentTypeOpt = masDepartmentTypeRepository.findById(id);
        if (departmentTypeOpt.isPresent()) {
            MasDepartmentType departmentType = departmentTypeOpt.get();
            departmentType.setStatus(status);
            departmentType.setLastChgDate(Instant.now());
            masDepartmentTypeRepository.save(departmentType);
            return ResponseUtils.createSuccessResponse("Department Type status updated to '" + status + "'", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Department Type not found", 404);
        }
    }

    @Override
    public ApiResponse<MasDepartmentTypeResponse> editDepartmentType(Long id, MasDepartmentTypeRequest request) {
        if (!isValidStatus(request.getStatus())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        Optional<MasDepartmentType> departmentTypeOpt = masDepartmentTypeRepository.findById(id);
        if (departmentTypeOpt.isPresent()) {
            MasDepartmentType departmentType = departmentTypeOpt.get();
            departmentType.setDepartmentTypeCode(request.getDepartmentTypeCode());
            departmentType.setDepartmentTypeName(request.getDepartmentTypeName());
            departmentType.setStatus(request.getStatus());
            departmentType.setLastChgBy(request.getLastChgBy());
            departmentType.setLastChgDate(Instant.now());
            masDepartmentTypeRepository.save(departmentType);
            return ResponseUtils.createSuccessResponse(mapToResponse(departmentType), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Department Type not found", 404);
        }
    }

    @Override
    public ApiResponse<MasDepartmentTypeResponse> getDepartmentTypeById(Long id) {
        Optional<MasDepartmentType> departmentTypeOpt = masDepartmentTypeRepository.findById(id);
        return departmentTypeOpt.map(departmentType -> ResponseUtils.createSuccessResponse(mapToResponse(departmentType), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Department Type not found", 404));
    }

    @Override
    public ApiResponse<List<MasDepartmentTypeResponse>> getAllDepartmentTypes() {
        List<MasDepartmentTypeResponse> departmentTypes = masDepartmentTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(departmentTypes, new TypeReference<>() {});
    }

    private MasDepartmentTypeResponse mapToResponse(MasDepartmentType departmentType) {
        MasDepartmentTypeResponse response = new MasDepartmentTypeResponse();
        response.setId(departmentType.getId());
        response.setDepartmentTypeCode(departmentType.getDepartmentTypeCode());
        response.setDepartmentTypeName(departmentType.getDepartmentTypeName());
        response.setStatus(departmentType.getStatus());
        response.setLastChgBy(departmentType.getLastChgBy());
        response.setLastChgDate(departmentType.getLastChgDate());
        return response;
    }
}