package com.hims.service;

import com.hims.entity.MasEmployee;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.*;
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

    ApiResponse<List<SpecialitiesAndDoctorResponse>> getDepartmentAndDoctor(String search);

    ApiResponse<List<SpecialityResponse>> getSpecialityAndDoctor(Long specialityId);

    ApiResponse<DoctorDetailResponse> getDoctor(Long doctorId);


    ApiResponse<List<AppointmentBookingHistoryResponseDetails>> appointmentHistory();
}