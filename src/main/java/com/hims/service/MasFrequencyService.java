package com.hims.service;

import com.hims.request.MasFrequencyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasFrequencyResponse;
import org.springframework.stereotype.Service;

import java.util.List;


public interface MasFrequencyService {
    ApiResponse<MasFrequencyResponse> createMasFrequency(MasFrequencyRequest masFrequencyRequest);


    ApiResponse<MasFrequencyResponse> updateMasFrequency(Long id, MasFrequencyRequest masFrequencyRequest);

    ApiResponse<MasFrequencyResponse> updateMasFrequencyByStatus(Long id, String status);

    ApiResponse<MasFrequencyResponse> getByIdMasFrequency(Long id);

    ApiResponse<List<MasFrequencyResponse>> getByAllMasFrequency(int flag);
}
