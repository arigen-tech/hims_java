package com.hims.service;

import com.hims.request.MasIdentificationTypeRequest;
import com.hims.response.MasIdentificationTypeResponse;
import com.hims.response.ApiResponse;
import java.util.List;

public interface MasIdentificationTypeService {
    ApiResponse<MasIdentificationTypeResponse> addIdentificationType(MasIdentificationTypeRequest request);
    ApiResponse<String> changeIdentificationStatus(Long id, String status);
    ApiResponse<MasIdentificationTypeResponse> editIdentificationType(Long id, MasIdentificationTypeRequest request);
    ApiResponse<MasIdentificationTypeResponse> getIdentificationTypeById(Long id);
    ApiResponse<List<MasIdentificationTypeResponse>> getAllIdentificationTypes(int flag);
}
