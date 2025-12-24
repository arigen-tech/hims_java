package com.hims.service;

import com.hims.request.EntMasTonsilGradeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntMasTonsilGradeResponse;

import java.util.List;

public interface EntMasTonsilGradeService {

    ApiResponse<List<EntMasTonsilGradeResponse>> getAll(int flag);

    ApiResponse<EntMasTonsilGradeResponse> getById(Long id);

    ApiResponse<EntMasTonsilGradeResponse> create(
            EntMasTonsilGradeRequest request);

    ApiResponse<EntMasTonsilGradeResponse> update(
            Long id, EntMasTonsilGradeRequest request);

    ApiResponse<EntMasTonsilGradeResponse> changeStatus(
            Long id, String status);
}
