package com.hims.service;

import com.hims.request.MasSubChargeCodeReq;
import com.hims.response.ApiResponse;
import com.hims.response.MasSubChargeCodeDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MasSubChargeCodeService {

   // @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<MasSubChargeCodeDTO> createSubCharge(MasSubChargeCodeReq codeReq);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<MasSubChargeCodeDTO> updateSubCharge(Long subId, MasSubChargeCodeReq codeReq);

    @Transactional(rollbackFor = {Exception.class})
    ApiResponse<MasSubChargeCodeDTO> changeStatus(Long subId, String status);

    ApiResponse<MasSubChargeCodeDTO> getBySubId(Long subId);
    ApiResponse<List<MasSubChargeCodeDTO>>getAllSubCharge(int flag);


}
