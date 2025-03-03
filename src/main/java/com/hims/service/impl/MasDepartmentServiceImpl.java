package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;
import com.hims.service.MasDepartmentService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasDepartmentServiceImpl implements MasDepartmentService {

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    @Override
    public ApiResponse<List<MasDepartmentResponse>> getAllDepartments() {
        List<MasDepartment> departments = masDepartmentRepository.findAll();

        List<MasDepartmentResponse> responses = departments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasDepartmentResponse convertToResponse(MasDepartment department) {
        MasDepartmentResponse response = new MasDepartmentResponse();
        response.setId(department.getId());
        response.setDepartmentCode(department.getDepartmentCode());
        response.setDepartmentName(department.getDepartmentName());
        response.setStatus(department.getStatus());
        response.setLastChgBy(department.getLastChgBy());
        response.setLastChgDate(department.getLastChgDate());
        response.setLastChgTime(department.getLastChgTime());
        response.setDepartmentNo(department.getDepartmentNo());

        if (department.getDepartmentType() != null) {
            response.setDepartmentTypeId(department.getDepartmentType().getId());
            response.setDepartmentTypeName(department.getDepartmentType().getDepartmentTypeName());
        }
        if (department.getHospital() != null) {
            response.setHospitalId(department.getHospital().getId());
            response.setHospitalName(department.getHospital().getHospitalName());
        }

        return response;
    }
}
