package com.hims.service;

import com.hims.entity.MasServiceOpd;
import com.hims.response.ApiResponse;

import java.util.List;

public interface MasServiceOpdService {
    ApiResponse<List<MasServiceOpd>> findByHospitalId(Long id);
}
