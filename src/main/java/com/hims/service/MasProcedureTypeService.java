package com.hims.service;

import com.hims.request.MasProcedureTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedureTypeResponse;

import java.util.List;

public interface MasProcedureTypeService {








    ApiResponse<List<MasProcedureTypeResponse>> getAllProcedureType(int flag);

    ApiResponse<MasProcedureTypeResponse> findById(Long id);

    ApiResponse<MasProcedureTypeResponse> addProcedureType(MasProcedureTypeRequest request);

    ApiResponse<MasProcedureTypeResponse> update(Long id, MasProcedureTypeRequest request);

    ApiResponse<MasProcedureTypeResponse> changeStatus(Long id, String status);
}
