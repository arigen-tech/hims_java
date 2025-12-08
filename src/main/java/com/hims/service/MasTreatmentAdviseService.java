package com.hims.service;

import com.hims.request.MasTreatmentAdviseRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTreatmentAdviseResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MasTreatmentAdviseService {
    ApiResponse<List<MasTreatmentAdviseResponse>> getAll(int flag);

    ApiResponse<MasTreatmentAdviseResponse> add(MasTreatmentAdviseRequest request);

    ApiResponse<MasTreatmentAdviseResponse> update(Long id, MasTreatmentAdviseRequest request);

    ApiResponse<MasTreatmentAdviseResponse> changeStatus(Long id, String status);
}
