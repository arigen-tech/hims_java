package com.hims.service;

import com.hims.request.MasInvestigationMethodologyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInvestigationMethodologyResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MasInvestigationMethodologyService {
    ApiResponse<String> create(MasInvestigationMethodologyRequest request);

    ApiResponse<List<MasInvestigationMethodologyResponse>> get();

    ApiResponse<String> update(Long id, MasInvestigationMethodologyRequest request);

    ApiResponse<MasInvestigationMethodologyResponse> findById(Long id);
}
