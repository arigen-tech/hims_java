package com.hims.service;

import com.hims.request.MasDepartmentTypeRequest;
import com.hims.response.MasDepartmentTypeResponse;
import com.hims.response.ApiResponse;
import java.util.List;

public interface MasDepartmentTypeService {
    ApiResponse<MasDepartmentTypeResponse> addDepartmentType(MasDepartmentTypeRequest request);
    ApiResponse<String> changeDepartmentTypeStatus(Long id, String status);
    ApiResponse<MasDepartmentTypeResponse> editDepartmentType(Long id, MasDepartmentTypeRequest request);
    ApiResponse<MasDepartmentTypeResponse> getDepartmentTypeById(Long id);
    ApiResponse<List<MasDepartmentTypeResponse>> getAllDepartmentTypes(int flag);
}