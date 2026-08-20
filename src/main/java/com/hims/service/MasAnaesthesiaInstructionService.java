package com.hims.service;

import com.hims.request.MasAnaesthesiaInstructionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasAnaesthesiaInstructionResponse;

import java.util.List;

public interface MasAnaesthesiaInstructionService {

    ApiResponse<List<MasAnaesthesiaInstructionResponse>> getAll(int flag);

    ApiResponse<MasAnaesthesiaInstructionResponse> getById(Long id);

    ApiResponse<MasAnaesthesiaInstructionResponse> create(MasAnaesthesiaInstructionRequest request);

    ApiResponse<MasAnaesthesiaInstructionResponse> update(Long id, MasAnaesthesiaInstructionRequest request);

    ApiResponse<MasAnaesthesiaInstructionResponse> changeStatus(Long id, String status);
}
