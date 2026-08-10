package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasReceiptTypeResponse;

import java.util.List;

public interface MasReceiptTypeService {
    public ApiResponse<List<MasReceiptTypeResponse>> getAll(int flag);
}
