package com.hims.service;

import com.hims.request.ObMasStationPresentingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasStationPresentingResponse;

import java.util.List;

public interface ObMasStationPresentingService {

    ApiResponse<List<ObMasStationPresentingResponse>> getAll(int flag);

    ApiResponse<ObMasStationPresentingResponse> getById(Long id);

    ApiResponse<ObMasStationPresentingResponse> create(
            ObMasStationPresentingRequest request);

    ApiResponse<ObMasStationPresentingResponse> update(
            Long id, ObMasStationPresentingRequest request);

    ApiResponse<ObMasStationPresentingResponse> changeStatus(
            Long id, String status);
}
