package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasDepartmentTypeRepository;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.request.MasDepartmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;
import com.hims.service.MasDepartmentService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class MasDepartmentServiceImpl implements MasDepartmentService {

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    @Autowired
    private MasDepartmentTypeRepository masDepartmentTypeRepository;

    @Autowired
    private MasHospitalRepository masHospitalRepository;

    private boolean isValidStatus(String status) {
        return "Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status);
    }

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<MasDepartmentResponse> addDepartment(MasDepartmentRequest request) {
        if (!isValidStatus(request.getStatus())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        MasDepartment department = new MasDepartment();
        department.setDepartmentCode(request.getDepartmentCode());
        department.setDepartmentName(request.getDepartmentName());
        department.setStatus(request.getStatus());
        department.setLastChgBy(request.getLastChgBy());
        department.setLastChgTime(getCurrentTimeFormatted());
        department.setLastChgDate(Instant.now());

        if (request.getDepartmentTypeId() != null) {
            department.setDepartmentType(masDepartmentTypeRepository.findById(request.getDepartmentTypeId()).orElse(null));
        }
        if (request.getHospitalId() != null) {
            department.setHospital(masHospitalRepository.findById(request.getHospitalId()).orElse(null));
        }
        department.setDepartmentNo(request.getDepartmentNo());

        MasDepartment savedDepartment = masDepartmentRepository.save(department);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedDepartment), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeDepartmentStatus(Long id, String status) {
        if (!isValidStatus(status)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        Optional<MasDepartment> departmentOpt = masDepartmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            MasDepartment department = departmentOpt.get();
            department.setStatus(status);
            department.setLastChgDate(Instant.now());
            masDepartmentRepository.save(department);
            return ResponseUtils.createSuccessResponse("Department status updated to '" + status + "'", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Department not found", 404);
        }
    }

    @Override
    public ApiResponse<MasDepartmentResponse> editDepartment(Long id, MasDepartmentRequest request) {
        Optional<MasDepartment> departmentOpt = masDepartmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            MasDepartment department = departmentOpt.get();
            department.setDepartmentCode(request.getDepartmentCode());
            department.setDepartmentName(request.getDepartmentName());
            department.setStatus(request.getStatus());
            department.setLastChgBy(request.getLastChgBy());
            department.setLastChgTime(getCurrentTimeFormatted());
            department.setLastChgDate(Instant.now());

            masDepartmentRepository.save(department);
            return ResponseUtils.createSuccessResponse(mapToResponse(department), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Department not found", 404);
        }
    }

    @Override
    public ApiResponse<MasDepartmentResponse> getDepartmentById(Long id) {
        return masDepartmentRepository.findById(id)
                .map(dept -> ResponseUtils.createSuccessResponse(mapToResponse(dept), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Department not found", 404));
    }

    @Override
    public ApiResponse<List<MasDepartmentResponse>> getAllDepartments() {
        List<MasDepartmentResponse> departments = masDepartmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(departments, new TypeReference<>() {});
    }

    private MasDepartmentResponse mapToResponse(MasDepartment department) {
        MasDepartmentResponse response = new MasDepartmentResponse();
        response.setId(department.getId());
        response.setDepartmentCode(department.getDepartmentCode());
        response.setDepartmentName(department.getDepartmentName());
        response.setStatus(department.getStatus());
        response.setLastChgBy(department.getLastChgBy());
        response.setLastChgDate(department.getLastChgDate());
        response.setLastChgTime(department.getLastChgTime());
        if (department.getDepartmentType() != null) {
            response.setDepartmentTypeId(department.getDepartmentType().getId());
            response.setDepartmentTypeName(department.getDepartmentType().getDepartmentTypeName());
        }
        if (department.getHospital() != null) {
            response.setHospitalId(department.getHospital().getId());
            response.setHospitalName(department.getHospital().getHospitalName());
        }
        response.setDepartmentNo(department.getDepartmentNo());
        return response;
    }

}
