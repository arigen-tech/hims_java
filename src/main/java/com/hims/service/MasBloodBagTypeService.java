package com.hims.service;

import com.hims.request.MasBloodBagTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodBagTypeResponse;

import java.util.List;

public interface MasBloodBagTypeService {

    ApiResponse<List<MasBloodBagTypeResponse>> getAll(int flag);

    ApiResponse<MasBloodBagTypeResponse> getById(Long id);

    ApiResponse<MasBloodBagTypeResponse> create(
            MasBloodBagTypeRequest request);

    ApiResponse<MasBloodBagTypeResponse> update(
            Long id, MasBloodBagTypeRequest request);

    ApiResponse<MasBloodBagTypeResponse> changeStatus(
            Long id, String status);
}
