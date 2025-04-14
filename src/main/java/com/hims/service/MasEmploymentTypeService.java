package com.hims.service;

import com.hims.entity.MasEmploymentType;
import com.hims.request.MasEmploymentTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmploymentTypeResponse;

import java.util.List;

public interface MasEmploymentTypeService
{

    ApiResponse<List<MasEmploymentTypeResponse>> getAllMasEmploymentType(int flag);

    ApiResponse<MasEmploymentTypeResponse> addMasEmploymentType(MasEmploymentTypeRequest masEmploymentTypeRequest);

    ApiResponse<MasEmploymentTypeResponse> getMasEmploymentTypeId(Long id);

    ApiResponse<MasEmploymentTypeResponse> updateMasEmploymentTypeById(MasEmploymentType masEmploymentType, Long id);

    ApiResponse<MasEmploymentTypeResponse> updateMasEmploymentTypeByStatus(Long id, String status);
}
