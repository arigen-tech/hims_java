package com.hims.service;

import com.hims.entity.MasUserType;
import com.hims.request.MasUserTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasUserTypeResponse;

import java.util.List;

public interface MasUserTypeService {
    ApiResponse<List<MasUserType>> getAllMasUserType(int flag);



    ApiResponse<MasUserTypeResponse> addMasUser(MasUserTypeRequest masUserTypeRequest);

    ApiResponse<MasUserTypeResponse> getByIdMasUserType(Long id);

    ApiResponse<MasUserTypeResponse> newAddMasUser(MasUserTypeRequest masUserTypeRequest);

    ApiResponse<MasUserTypeResponse> updateMasUserType(MasUserType masUserType, Long id);

    ApiResponse<MasUserTypeResponse> updateMasUserTypeStatus(Long id, String status);
}
