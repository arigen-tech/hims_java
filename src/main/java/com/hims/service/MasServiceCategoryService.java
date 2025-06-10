package com.hims.service;

import com.hims.entity.MasServiceCategory;
import com.hims.response.ApiResponse;

import java.util.List;

public interface MasServiceCategoryService {
    ApiResponse<List<MasServiceCategory>> findAll(int flag);
    ApiResponse<MasServiceCategory> save(MasServiceCategory req);
    ApiResponse<MasServiceCategory> edit(MasServiceCategory req);

}
