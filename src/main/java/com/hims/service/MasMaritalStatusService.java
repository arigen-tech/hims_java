package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasMaritalStatusResponse;

import java.util.List;

public interface MasMaritalStatusService {
    ApiResponse<List<MasMaritalStatusResponse>> getAllMaritalStatuses();
}
