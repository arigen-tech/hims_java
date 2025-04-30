package com.hims.service;

import com.hims.request.DgInvestigationPackageRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgInvestigationPackageDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DgInvestigationPackageServices {

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgInvestigationPackageDTO> createInvestPack(DgInvestigationPackageRequest packReq);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<DgInvestigationPackageDTO> updateInvestPack(Long packId, DgInvestigationPackageRequest packReq);

    @Transactional(rollbackFor = {Exception.class})
    ApiResponse<DgInvestigationPackageDTO> changeStatus(Long packId, String status);

    ApiResponse<DgInvestigationPackageDTO> getByPackId(Long packId);
    ApiResponse<List<DgInvestigationPackageDTO>>getAllPackInvestigation(int flag);

}
