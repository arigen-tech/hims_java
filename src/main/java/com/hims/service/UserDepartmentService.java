package com.hims.service;

import com.hims.request.UserDepartmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.UserDepartmentResponse;

import java.util.List;

public interface UserDepartmentService {

    ApiResponse<List<UserDepartmentResponse>> getAllUserDepartments();
    ApiResponse<UserDepartmentResponse> addUserDepartment(UserDepartmentRequest request);
    ApiResponse<UserDepartmentResponse> updateUserDepartment(Long id, UserDepartmentResponse details);
    ApiResponse<UserDepartmentResponse> findById(Long id);

}
