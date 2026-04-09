package com.hims.service;

import com.hims.request.IpdPackageRequest;
import com.hims.response.ApiResponse;
import com.hims.response.IpdPackageDetailsResponse;
import com.hims.response.IpdPackageResponse;

import java.util.List;

public interface MasIpdPackageService {
    ApiResponse<String> savePackage(IpdPackageRequest request);

    ApiResponse<List<IpdPackageResponse>> getAllIpdPackages(int flag);

    ApiResponse<IpdPackageResponse> changeStatus(Long id, String status);

    ApiResponse<IpdPackageDetailsResponse> getById(Long id);

    ApiResponse<String> updatePackage(Long id, IpdPackageRequest request);
}
