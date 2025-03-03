package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasCountryResponse;

import java.util.List;

public interface MasCountryService {
    ApiResponse<List<MasCountryResponse>> getAllCountries();
}
