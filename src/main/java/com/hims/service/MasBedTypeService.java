package com.hims.service;

import com.hims.request.MasBedTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBedTypeResponse;
import com.hims.response.MasRoomCategoryResponse;
import org.springframework.stereotype.Service;

@Service
public interface MasBedTypeService {
    Object createRoomCategory(MasBedTypeRequest request);

    Object updateRoomCategory(Long id, MasBedTypeRequest request);

    ApiResponse<MasBedTypeResponse> changeActiveStatus(Long id, String status);

    Object getById(Long id);

    Object getAll(int flag);
}
