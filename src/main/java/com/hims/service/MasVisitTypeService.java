package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasVisitTypeResponse;

import java.util.List;

public interface MasVisitTypeService {
    ApiResponse<List<MasVisitTypeResponse>> getAll(int flag);
}
