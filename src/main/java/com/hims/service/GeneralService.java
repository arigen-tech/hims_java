package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.ModalityDetailsByDepartmentResponse;

import java.util.List;

public interface GeneralService {
    ApiResponse<List<ModalityDetailsByDepartmentResponse>> getModalityDetailsByDepartment(String code);
}
