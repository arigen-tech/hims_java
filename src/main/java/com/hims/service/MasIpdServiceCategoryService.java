package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.IpdServiceCategoryResponse;

import java.util.List;

public interface MasIpdServiceCategoryService {
    ApiResponse<List<IpdServiceCategoryResponse>> getAll(int flag);
}
