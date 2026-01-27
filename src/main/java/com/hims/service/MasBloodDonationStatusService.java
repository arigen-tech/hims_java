package com.hims.service;

import com.hims.request.MasBloodDonationStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodDonationStatusResponse;

import java.util.List;

public interface MasBloodDonationStatusService {

    ApiResponse<List<MasBloodDonationStatusResponse>> getAll(int flag);

    ApiResponse<MasBloodDonationStatusResponse> getById(Long id);

    ApiResponse<MasBloodDonationStatusResponse> create(
            MasBloodDonationStatusRequest request);

    ApiResponse<MasBloodDonationStatusResponse> update(
            Long id, MasBloodDonationStatusRequest request);

    ApiResponse<MasBloodDonationStatusResponse> changeStatus(
            Long id, String status);
}
