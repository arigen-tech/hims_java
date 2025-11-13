package com.hims.service;

import com.hims.request.ResultEntryMainRequest;
import com.hims.request.ResultUpdateRequest;
import com.hims.request.ResultValidationUpdateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgResultEntryValidationResponse;
import com.hims.response.ResultEntryUpdateResponse;

import java.util.List;

public interface ResultService {
   ApiResponse<String> saveOrUpdateResultEntry(ResultEntryMainRequest request);

   ApiResponse<List<DgResultEntryValidationResponse>> getUnvalidatedResults();

   ApiResponse<String> updateResultValidation( ResultValidationUpdateRequest request);

   ApiResponse<List<ResultEntryUpdateResponse>> getUpdate();

  // ApiResponse<String> updateResult(ResultUpdateRequest request);
}
