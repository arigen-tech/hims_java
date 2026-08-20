package com.hims.service;

import com.hims.request.MasOtScheduleChangeReasonRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtScheduleChangeReasonResponse;

import java.util.List;

public interface MasOtScheduleChangeReasonService {

    ApiResponse<List<MasOtScheduleChangeReasonResponse>> getAll(int flag);

    ApiResponse<MasOtScheduleChangeReasonResponse> getById(Long id);

    ApiResponse<MasOtScheduleChangeReasonResponse> create(MasOtScheduleChangeReasonRequest request);

    ApiResponse<MasOtScheduleChangeReasonResponse> update(Long id, MasOtScheduleChangeReasonRequest request);

    ApiResponse<MasOtScheduleChangeReasonResponse> changeStatus(Long id, String status);
}
