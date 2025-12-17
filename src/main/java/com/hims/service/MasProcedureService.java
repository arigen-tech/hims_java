package com.hims.service;

import com.hims.request.MasProcedureRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasProcedureResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MasProcedureService {
    ApiResponse<List<MasProcedureResponse>> getAllMasProcedure(int flag);

    ApiResponse<Page<MasProcedureResponse>> getAllProceduresWIthFilter(
            int flag, int page, int size, String search);

    ApiResponse<MasProcedureResponse> getMasProcedureById(Long id);

    ApiResponse<MasProcedureResponse> addMasProcedure(MasProcedureRequest request);

    ApiResponse<MasProcedureResponse> updateMasProcedure(Long id, MasProcedureRequest request);

    ApiResponse<MasProcedureResponse> changeStatus(Long id, String status);
}
