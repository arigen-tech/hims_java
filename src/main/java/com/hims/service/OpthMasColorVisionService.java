package com.hims.service;

import com.hims.request.OpthMasColorVisionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpthMasColorVisionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface OpthMasColorVisionService {

    ApiResponse<List<OpthMasColorVisionResponse>> getAll(int flag);

    ApiResponse<OpthMasColorVisionResponse> getById(Long id);

    ApiResponse<OpthMasColorVisionResponse> create(
            OpthMasColorVisionRequest request);

    ApiResponse<OpthMasColorVisionResponse> update(
            Long id, OpthMasColorVisionRequest request);

    ApiResponse<OpthMasColorVisionResponse> changeStatus(
            Long id, String status);
}
