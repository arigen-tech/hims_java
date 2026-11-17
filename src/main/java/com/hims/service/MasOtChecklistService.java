package com.hims.service;

import com.hims.request.MasOtChecklistRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtChecklistResponse;
import org.springframework.data.domain.Page;

public interface MasOtChecklistService {
    ApiResponse<MasOtChecklistResponse> save(MasOtChecklistRequest request);
    ApiResponse<MasOtChecklistResponse> getById(Long id);
    ApiResponse<Page<MasOtChecklistResponse>> getAll(int flag, String checklistStage, String checklistName, int page, int size);
    ApiResponse<MasOtChecklistResponse> changeStatus(Long id, String status);
}
