package com.hims.service;

import com.hims.request.ResultEntryMainRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgResultEntryValidationResponse;

import java.util.List;

public interface ResultService {
   ApiResponse<String> saveOrUpdateResultEntry(ResultEntryMainRequest request);

   ApiResponse<List<DgResultEntryValidationResponse>> getUnvalidatedResults();
}
