package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasBloodDiscardReasonRes;

import java.util.List;

public interface MasBloodDiscardReasonService {

    ApiResponse<List<MasBloodDiscardReasonRes>> getAllReasons(int flag);
}
