package com.hims.service;

import com.hims.request.MasProcedureRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedureResponse;

import java.util.List;

public interface MasProcedureService {
    ApiResponse<List<MasProcedureResponse>> getAllMasProcedure(int flag);

    ApiResponse<MasProcedureResponse> getMasProcedureById(Integer id);

    ApiResponse<MasProcedureResponse> addMasProcedure(MasProcedureRequest request);

    ApiResponse<MasProcedureResponse> updateMasProcedure(Integer id, MasProcedureRequest request);

    ApiResponse<MasProcedureResponse> changeStatus(Integer id, String status);
}
