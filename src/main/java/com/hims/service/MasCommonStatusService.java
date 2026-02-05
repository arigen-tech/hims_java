package com.hims.service;

import com.hims.request.MasCommonStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntityNameResponse;
import com.hims.response.MasCommonStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MasCommonStatusService {

    ApiResponse<MasCommonStatusResponse> createCommonStatus(MasCommonStatusRequest request);

    ApiResponse<MasCommonStatusResponse> updateCommonStatusById(Long statusId, MasCommonStatusRequest request);

    ApiResponse<MasCommonStatusResponse> getCommonStatusById(Long statusId);

    ApiResponse<List<MasCommonStatusResponse>> getAllCommonStatus();

    Page<EntityNameResponse> searchEntities(String keyword, Pageable pageable);

    ApiResponse<List<String>> getColumnNamesFromEntity(String entityName);


    ApiResponse<String> getTableNameForEntity(String entityName);
}