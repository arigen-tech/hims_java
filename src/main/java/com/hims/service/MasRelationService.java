package com.hims.service;

import com.hims.entity.MasRelation;
import com.hims.request.MasRelationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRelationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface MasRelationService {
    ApiResponse<List<MasRelationResponse>> getAllRelations(int flag);
    public ApiResponse<MasRelationResponse> changeStatus(Long id, String status);
    public ApiResponse<MasRelationResponse> findById(Long id);
    public ApiResponse<MasRelationResponse> addRelation(MasRelationRequest relationRequest);
    public ApiResponse<MasRelationResponse> updateRelation(Long id, MasRelationRequest relationRequest);
}
