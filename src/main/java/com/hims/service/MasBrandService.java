package com.hims.service;

import com.hims.request.MasBrandRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBrandResponse;

import java.util.List;

public interface MasBrandService {
    ApiResponse<List<MasBrandResponse>> getAllMasBrand(int flag);


    ApiResponse<MasBrandResponse> addMasBrand(MasBrandRequest masBrandRequest);

    ApiResponse<MasBrandResponse> update(Long id, MasBrandRequest request);

    ApiResponse<MasBrandResponse> findById(Long id);

    ApiResponse<MasBrandResponse> changeMasBrandStatus(Long id, String status);
}
