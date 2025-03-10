package com.hims.service;

import com.hims.request.MasCountryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCountryResponse;

import java.util.List;

public interface MasCountryService {
    ApiResponse<MasCountryResponse> addCountry(MasCountryRequest request);
    ApiResponse<String> changeCountryStatus(Long id, String status);
    ApiResponse<MasCountryResponse> editCountry(Long id, MasCountryRequest request);
    ApiResponse<MasCountryResponse> getCountryById(Long id);
    ApiResponse<List<MasCountryResponse>> getAllCountries();
}
