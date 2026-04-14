package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasTpaResponse;

import java.util.List;

public interface MasTpaService {
    ApiResponse<List<MasTpaResponse>> getAllMasTpa(int flag);
}
