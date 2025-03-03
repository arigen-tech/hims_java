package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;

import java.util.List;

public interface MasDepartmentService {
    ApiResponse<List<MasDepartmentResponse>> getAllDepartments();
}
