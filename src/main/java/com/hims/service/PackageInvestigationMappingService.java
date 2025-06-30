package com.hims.service;

import com.hims.request.PackageInvestigationMappingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PackageInvestigationMappingDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PackageInvestigationMappingService {

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<PackageInvestigationMappingDTO> createPackMap(PackageInvestigationMappingRequest mapRequest);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<PackageInvestigationMappingDTO> updatePackMap(Long pimId, PackageInvestigationMappingRequest mapRequest);

    @Transactional(rollbackFor = {Exception.class})
    ApiResponse<PackageInvestigationMappingDTO> changeStatus(Long pimId, String status);

    ApiResponse<PackageInvestigationMappingDTO> getByPimId(Long pimId);
    ApiResponse<List<PackageInvestigationMappingDTO>>getAllPackageMap(int flag);
}
