package com.hims.service;

import com.hims.request.ResultEntryMainRequest;
import com.hims.response.ApiResponse;

public interface ResultService {
   ApiResponse<String> saveOrUpdateResultEntry(ResultEntryMainRequest request);
}
