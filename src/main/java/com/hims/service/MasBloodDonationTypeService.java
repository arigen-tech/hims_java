package com.hims.service;

import com.hims.request.MasBloodDonationTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodDonationTypeResponse;

import java.util.List;
public interface MasBloodDonationTypeService {

    ApiResponse<List<MasBloodDonationTypeResponse>> getAll(int flag);

    ApiResponse<MasBloodDonationTypeResponse> getById(Long id);

    ApiResponse<MasBloodDonationTypeResponse> create(MasBloodDonationTypeRequest request);

    ApiResponse<MasBloodDonationTypeResponse> update(Long id, MasBloodDonationTypeRequest request);

    ApiResponse<MasBloodDonationTypeResponse> changeStatus(Long id, String status);
}
