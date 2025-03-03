package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasRelationResponse;

import java.util.List;

public interface MasRelationService {
    ApiResponse<List<MasRelationResponse>> getAllRelations();
}
