package com.hims.service;

import com.hims.entity.MasUserDepartment;
import com.hims.request.MasDepartmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;
import com.hims.response.MasUserDepartmentResponse;

import java.util.List;

public interface MasDepartmentService {
    ApiResponse<MasDepartmentResponse> addDepartment(MasDepartmentRequest request);
    ApiResponse<String> changeDepartmentStatus(Long id, String status);
    ApiResponse<MasDepartmentResponse> editDepartment(Long id, MasDepartmentRequest request);
    ApiResponse<MasDepartmentResponse> getDepartmentById(Long id);
    ApiResponse<List<MasDepartmentResponse>> getAllDepartments(int flag);


    ApiResponse<List<MasUserDepartmentResponse>> getAllMasUserDepartments();

    ApiResponse<List<MasUserDepartmentResponse>> getMasUserDepartmentsByDepartmentId(Long departmentId);

    ApiResponse<List<MasUserDepartmentResponse>> getMasUserDepartmentsByUserId(Long userId);

    ApiResponse<List<MasDepartmentResponse>> getAllWardDepartmentByWardCategory(Long wardCategory);
}
