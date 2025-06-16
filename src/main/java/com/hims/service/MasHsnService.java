package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasHsnResponse;

import java.util.List;

public interface MasHsnService {
    ApiResponse<List<MasHsnResponse>> getAllMasStoreItem(int flag);
}
