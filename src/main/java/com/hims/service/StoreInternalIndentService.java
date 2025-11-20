package com.hims.service;

import com.hims.entity.MasDepartment;
import com.hims.request.StoreInternalIndentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.StoreInternalIndentResponse;

import java.util.List;

public interface StoreInternalIndentService {
    // Save (draft) — backend sets status "S"
    ApiResponse<StoreInternalIndentResponse> saveIndent(StoreInternalIndentRequest request);

    // Submit — backend sets status "Y"
    ApiResponse<StoreInternalIndentResponse> submitIndent(Long indentMId);

    ApiResponse<StoreInternalIndentResponse> getIndentById(Long indentMId);

    ApiResponse<List<StoreInternalIndentResponse>> listIndentsByCurrentDept(String status);

    // Import endpoints
    ApiResponse<StoreInternalIndentResponse> createIndentFromROL(StoreInternalIndentRequest baseRequest);
    ApiResponse<StoreInternalIndentResponse> createIndentFromPrevious(Long previousIndentMId);

    public List<MasDepartment> getOtherFixedDepartmentsForCurrentUser();
}
