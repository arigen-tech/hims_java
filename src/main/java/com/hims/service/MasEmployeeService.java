package com.hims.service;

import com.hims.entity.MasEmployee;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmployeeDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface MasEmployeeService {
    ApiResponse<List<MasEmployeeDTO>> getAllEmployees();

    ApiResponse<List<MasEmployeeDTO>> getEmployeesByStatus(String status);


    ApiResponse<MasEmployee> createEmployee(MasEmployeeRequest masEmployeeRequest);

    ApiResponse<MasEmployeeDTO> getEmployeeById(Long id);

    ApiResponse<MasEmployee> updateEmployee(Long id, MasEmployeeRequest masEmployeeRequest);
    @Transactional(rollbackFor = {Exception.class})
    ApiResponse<MasEmployee> updateEmployeeApprovalStatus(Long empId, Long deptId);
    @Transactional(rollbackFor = {Exception.class})
    ApiResponse<MasEmployee> createAndApproveEmployee(MasEmployeeRequest masEmployeeRequest);
}