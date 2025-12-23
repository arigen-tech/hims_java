package com.hims.service;

import com.hims.request.ObMasImmunisedStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasImmunisedStatusResponse;

import java.util.List;

public interface ObMasImmunisedStatusService {

    ApiResponse<List<ObMasImmunisedStatusResponse>> getAll(int flag);

    ApiResponse<ObMasImmunisedStatusResponse> getById(Long id);

    ApiResponse<ObMasImmunisedStatusResponse> create(
            ObMasImmunisedStatusRequest request);

    ApiResponse<ObMasImmunisedStatusResponse> update(
            Long id, ObMasImmunisedStatusRequest request);

    ApiResponse<ObMasImmunisedStatusResponse> changeStatus(
            Long id, String status);
}
