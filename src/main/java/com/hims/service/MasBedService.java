package com.hims.service;

import com.hims.request.MasBedRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBedResponse;
import org.springframework.stereotype.Service;

@Service
public interface MasBedService {

    ApiResponse<?> createRoomCategory(MasBedRequest request);

    ApiResponse<?> updateRoomCategory(Long id, MasBedRequest request);

    ApiResponse<MasBedResponse> changeActiveStatus(Long id, String status);

    ApiResponse<?> getById(Long id);

    ApiResponse<?> getAll(int flag);
}
