package com.hims.service;

import com.hims.entity.MasInvestigationCategory;
import com.hims.request.MasInvestigationCategoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInvestigationCategoryResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MasInvestigationCategoryService {
    ApiResponse<String> create(MasInvestigationCategoryRequest request);

    ApiResponse<List<MasInvestigationCategoryResponse>> get();

    ApiResponse<String> update(Long id, MasInvestigationCategoryRequest request);

    ApiResponse<MasInvestigationCategoryResponse> findById(Long id);
}
