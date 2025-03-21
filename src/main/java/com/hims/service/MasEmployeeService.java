package com.hims.service;

import com.hims.entity.MasEmployee;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmployeeDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface MasEmployeeService {
    ApiResponse<List<MasEmployeeDTO>> getAllEmployees();
    ApiResponse<MasEmployee> getEmployeeById(Long id);
    ApiResponse<MasEmployee> createEmployee(MasEmployeeRequest masEmployeeRequest);
    ApiResponse<MasEmployee> updateEmployee(Long id, MasEmployeeRequest masEmployeeRequest);
    ApiResponse<MasEmployee> updateEmployeeStatus(Long empId,String status);


    @Transactional(rollbackFor = {Exception.class})
    ApiResponse<MasEmployee> updateEmployeeApprovalStatus(Long empId);

    @Transactional(rollbackFor = {Exception.class})
    ApiResponse<MasEmployee> createAndApproveEmployee(MasEmployeeRequest masEmployeeRequest);
}