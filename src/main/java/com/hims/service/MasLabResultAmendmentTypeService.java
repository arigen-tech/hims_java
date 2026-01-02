package com.hims.service;

import com.hims.request.MasLabResultAmendmentTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasLabResultAmendmentTypeResponse;

import java.util.List;

public interface MasLabResultAmendmentTypeService {

    ApiResponse<MasLabResultAmendmentTypeResponse> create(MasLabResultAmendmentTypeRequest request);

    ApiResponse<MasLabResultAmendmentTypeResponse> update(Long amendmentTypeId, MasLabResultAmendmentTypeRequest request);

    ApiResponse<MasLabResultAmendmentTypeResponse> changeActiveStatus(Long amendmentTypeId, String status);

    ApiResponse<MasLabResultAmendmentTypeResponse> getById(Long amendmentTypeId);

    ApiResponse<List<MasLabResultAmendmentTypeResponse>> getAll(int flag);
}
