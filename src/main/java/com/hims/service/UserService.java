package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.UserResponse;

import java.util.List;

public interface UserService {
    ApiResponse<List<UserResponse>> getAllDoctorsBySpeciality(Long speciality);
}
