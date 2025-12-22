package com.hims.service;

import com.hims.request.ObMasPvMembraneRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ObMasPvMembraneResponse;

import java.util.List;

public interface ObMasPvMembraneService {

    ApiResponse<List<ObMasPvMembraneResponse>> getAll(int flag);

    ApiResponse<ObMasPvMembraneResponse> getById(Long id);

    ApiResponse<ObMasPvMembraneResponse> create(
            ObMasPvMembraneRequest request);

    ApiResponse<ObMasPvMembraneResponse> update(
            Long id, ObMasPvMembraneRequest request);

    ApiResponse<ObMasPvMembraneResponse> changeStatus(
            Long id, String status);
}
