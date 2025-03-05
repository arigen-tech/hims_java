package com.hims.service;

import com.hims.request.MasDepartmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;

import java.util.List;

public interface MasDepartmentService {
    ApiResponse<MasDepartmentResponse> addDepartment(MasDepartmentRequest request);
    ApiResponse<String> changeDepartmentStatus(Long id, String status);
    ApiResponse<MasDepartmentResponse> editDepartment(Long id, MasDepartmentRequest request);
    ApiResponse<MasDepartmentResponse> getDepartmentById(Long id);
    ApiResponse<List<MasDepartmentResponse>> getAllDepartments();
}
