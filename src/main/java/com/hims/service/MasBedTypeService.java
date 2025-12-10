package com.hims.service;

import com.hims.request.MasBedTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBedTypeResponse;
import com.hims.response.MasRoomCategoryResponse;
import org.springframework.stereotype.Service;

@Service
public interface MasBedTypeService {
    ApiResponse<?> masBedTypeCreate(MasBedTypeRequest request);

    ApiResponse<?> masBedTypeUpdate(Long id, MasBedTypeRequest request);

    ApiResponse<MasBedTypeResponse> changeActiveStatus(Long id, String status);

    ApiResponse<?> getById(Long id);

    ApiResponse<?> getAll(int flag);
}
