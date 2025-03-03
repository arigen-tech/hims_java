package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasStateResponse;

import java.util.List;

public interface MasStateService {
    ApiResponse<List<MasStateResponse>> getAllStates();
}
