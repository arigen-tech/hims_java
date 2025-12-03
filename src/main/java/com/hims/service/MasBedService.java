package com.hims.service;

import com.hims.request.MasBedRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBedResponse;
import org.springframework.stereotype.Service;

@Service
public interface MasBedService {

    Object createRoomCategory(MasBedRequest request);

    Object updateRoomCategory(Long id, MasBedRequest request);

    ApiResponse<MasBedResponse> changeActiveStatus(Long id, String status);

    Object getById(Long id);

    Object getAll(int flag);
}
