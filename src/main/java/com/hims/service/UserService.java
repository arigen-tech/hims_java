package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.UserResponse;

import java.util.List;

public interface UserService {
    ApiResponse<List<UserResponse>> getAllDoctorsBySpeciality(Long speciality);

    ApiResponse<List<UserResponse>> getAllDoctors();

    ApiResponse<List<UserResponse>> getAllUsers(int flag);

    ApiResponse<UserResponse> findByUser(String user);
}
