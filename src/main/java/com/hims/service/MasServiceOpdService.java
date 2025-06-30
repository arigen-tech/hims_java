package com.hims.service;

import com.hims.entity.MasServiceOpd;
import com.hims.request.MasServiceOpdRequest;
import com.hims.response.ApiResponse;

import java.util.List;

public interface MasServiceOpdService {
    ApiResponse<List<MasServiceOpd>> findByHospitalId(Long id);


    ApiResponse<MasServiceOpd> save(MasServiceOpdRequest req);


    ApiResponse<MasServiceOpd> edit(Long id, MasServiceOpdRequest req);

    ApiResponse<MasServiceOpd> updateStatus(Long id, String status);
}
