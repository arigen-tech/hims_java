package com.hims.service;

import com.hims.request.MasConsentTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasConsentTypeResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public interface MasConsentTypeService {
    ApiResponse<MasConsentTypeResponse> create(MasConsentTypeRequest request);
    ApiResponse<MasConsentTypeResponse> update(Long id, MasConsentTypeRequest request);
    ApiResponse<MasConsentTypeResponse> getById(Long id);
    ApiResponse<List<MasConsentTypeResponse>> getAll(int flag, String search);
    ApiResponse<Page<MasConsentTypeResponse>> getAll(int flag, String search, int page, int size);
    ApiResponse<MasConsentTypeResponse> changeStatus(Long id, String status);
}
