package com.hims.service;

import com.hims.entity.MasMainChargeCode;
import com.hims.request.MasMainChargeCodeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMainChargeCodeDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MasMainChargeCodeService {
    ApiResponse<List<MasMainChargeCodeDTO>> getAllChargeCode(int flag);
    ApiResponse<MasMainChargeCodeDTO> getChargeCodeById(Long chargecodeId);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<MasMainChargeCodeDTO> createChargeCode(MasMainChargeCodeRequest codeRequest);

    @Transactional(rollbackFor = {Exception.class})
    public ApiResponse<MasMainChargeCodeDTO> updateChargeCode(Long chargecodeId, MasMainChargeCodeRequest codeRequest);

    @Transactional(rollbackFor = {Exception.class})
    ApiResponse<MasMainChargeCodeDTO> changeStatus(Long chargecodeId, String status);
}
