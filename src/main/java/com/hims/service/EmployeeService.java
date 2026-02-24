package com.hims.service;

import com.hims.entity.MasEmployee;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmployeeDTO;

import java.util.List;

public interface EmployeeService {

    ApiResponse<List<MasEmployeeDTO>> getAllEmployees();

    ApiResponse<List<MasEmployeeDTO>> getEmployeesByStatus(String status);

    ApiResponse<MasEmployeeDTO> getEmployeeById(Long id);

    ApiResponse<?> createEmployee(MasEmployeeRequest request);

    ApiResponse<MasEmployee> updateEmployee(Long id, MasEmployeeRequest request);

    ApiResponse<MasEmployee> updateEmployeeApprovalStatus(Long id, Long deptId);

    ApiResponse<MasEmployee> createAndApproveEmployee(MasEmployeeRequest request);
}

