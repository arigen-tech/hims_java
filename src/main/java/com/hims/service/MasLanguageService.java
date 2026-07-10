package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasLanguageResponse;

import java.util.List;

public interface MasLanguageService {
    ApiResponse<List<MasLanguageResponse>> getAll(int flag);
}
