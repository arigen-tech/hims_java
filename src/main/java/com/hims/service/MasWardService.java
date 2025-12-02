package com.hims.service;

import com.hims.request.MasWardRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasWardResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MasWardService {
    ApiResponse<List<MasWardResponse>> getAllMasWardCategory(int flag);

    ApiResponse<MasWardResponse> findById(Long id);

    ApiResponse<MasWardResponse> addMasWard(MasWardRequest request);

    ApiResponse<MasWardResponse> update(Long id, MasWardRequest request);

    ApiResponse<MasWardResponse> changeMasWardStatus(Long id, String status);
}
