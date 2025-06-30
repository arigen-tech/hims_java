package com.hims.service;

import com.hims.request.UserDepartmentRequest;
import com.hims.request.UserDepartmentRequestOne;
import com.hims.response.ApiResponse;
import com.hims.response.UserDepartmentResponse;

import java.util.List;

public interface UserDepartmentService {

    ApiResponse<List<UserDepartmentResponse>> getAllUserDepartments();
    ApiResponse<UserDepartmentResponse> addUserDepartment(UserDepartmentRequest request);
    ApiResponse<UserDepartmentResponse> updateUserDepartment(Long id, UserDepartmentResponse details);
    ApiResponse<UserDepartmentResponse> findById(Long id);

    ApiResponse<String> addOrUpdateUserDept(UserDepartmentRequestOne request);

    ApiResponse<List<UserDepartmentResponse>> getAllUserDepartmentsByUserId(Long userId);

    ApiResponse<List<UserDepartmentResponse>> getAllUserDepartmentsByUserUserName(String userName);
}
