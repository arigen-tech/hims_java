package com.hims.service;

import com.hims.entity.MasEmployee;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmployeeDTO;
import com.hims.response.MasEmployeeResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EmployeeService {

    ApiResponse<Page<MasEmployeeResponse>> getAllEmployees(
            String employeeName,
            String mobileNo,
            int page,
            int size
    );
    ApiResponse<List<MasEmployeeDTO>> getEmployeesByStatus(String status);

    ApiResponse<MasEmployeeDTO> getEmployeeById(Long id);

    ApiResponse<?> createEmployee(MasEmployeeRequest request);

    ApiResponse<MasEmployeeDTO> updateEmployee(Long id, MasEmployeeRequest request);

    ApiResponse<MasEmployee> updateEmployeeApprovalStatus(Long id, Long deptId);

    ApiResponse<MasEmployee> createAndApproveEmployee(MasEmployeeRequest request);
}

