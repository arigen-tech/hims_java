package com.hims.service;

import com.hims.request.MasSymptomsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSymptomsResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MasSymptomsService {
    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<MasSymptomsResponse> createSymptom (MasSymptomsRequest symptomsReq);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<MasSymptomsResponse> updateSymptom (Long id, MasSymptomsRequest symptomsReq);

    public ApiResponse<MasSymptomsResponse> changeSymptomStatus (Long id, String status);

    public ApiResponse<MasSymptomsResponse> findBySymptomId (Long id);

    public ApiResponse<List<MasSymptomsResponse>> getAllSymptoms (int flag);
}
