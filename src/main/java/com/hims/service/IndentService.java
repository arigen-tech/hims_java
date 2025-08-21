package com.hims.service;

import com.hims.request.IndentRequest;
import com.hims.response.ApiResponse;

public interface IndentService {
    ApiResponse<String> createIndent(IndentRequest indentRequest);
}
