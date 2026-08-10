package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasPaymentModeResponse;

import java.util.List;

public interface MasPaymentModeService {
    ApiResponse<List<MasPaymentModeResponse>> getAll(int flag);
}
