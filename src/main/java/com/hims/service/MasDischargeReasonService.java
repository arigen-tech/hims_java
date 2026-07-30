package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasDischargeReasonResponse;

import java.util.List;

public interface MasDischargeReasonService {
    ApiResponse<List<MasDischargeReasonResponse>> getAll(int flag);
}
