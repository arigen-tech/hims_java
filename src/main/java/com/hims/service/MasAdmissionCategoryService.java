package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionCategoryResponse;

import java.util.List;

public interface MasAdmissionCategoryService{
    ApiResponse<List<MasAdmissionCategoryResponse>> getAllMasAdmissionCategory(int flag);
}
