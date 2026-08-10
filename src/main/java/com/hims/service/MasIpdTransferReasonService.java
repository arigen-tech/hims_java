package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasIpdTransferReasonResponse;

import java.util.List;

public interface MasIpdTransferReasonService {
    ApiResponse<List<MasIpdTransferReasonResponse>> getAll(int flag);
}
