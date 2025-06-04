package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.DgMasInvestigationResponse;

import java.util.List;

public interface DgMasInvestigationService {
    ApiResponse<List<DgMasInvestigationResponse>> getPriceDetails(String genderApplicable);

    ApiResponse<List<DgMasInvestigationResponse>> getAllInvestigations(int flag);



}
