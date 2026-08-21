package com.hims.service;

import com.hims.entity.MasManufacturer;
import com.hims.request.MasManufacturerRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasManufacturerResponse;

import java.util.List;

public interface MasManufacturerService {
    ApiResponse<List<MasManufacturer>> getAllMasManufacturer(int flag);

    ApiResponse<MasManufacturer> findById(Long id);

    ApiResponse<MasManufacturer> addMasManufacturer(MasManufacturerRequest masManufacturerRequest);

    ApiResponse<MasManufacturer> changeMasManufacturer(Long id, String status);

    ApiResponse<MasManufacturer> update(Long id, MasManufacturerRequest request);

    ApiResponse<List<MasManufacturerResponse>> getMasManufacturerWrtItemTypeCode(String itemTypeCode);
}
