package com.hims.service;

import com.hims.request.PackageRateConfigRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PackageRateConfigResponse;
import org.springframework.data.domain.Page;

public interface PackageRateConfigService {
    ApiResponse<PackageRateConfigResponse> savePackageRateConfig(PackageRateConfigRequest request);

    ApiResponse<PackageRateConfigResponse> updatePackageRateConfig(Long id, PackageRateConfigRequest request);

    ApiResponse<PackageRateConfigResponse> changeStatus(Long id, String status);

    ApiResponse<PackageRateConfigResponse> getByIdPackageRateConfig(Long id);

    ApiResponse<Page<PackageRateConfigResponse>> getByAllPackageRateConfigId(Long billingTypeId, Long corporateId, Long insuranceId, String search, int page, int size);
}
