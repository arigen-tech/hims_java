package com.hims.service;


import com.hims.request.MasQuestionOptionValueRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasQuestionOptionValueResponse;

import java.util.List;

public interface MasQuestionOptionValueService {

    ApiResponse<List<MasQuestionOptionValueResponse>> getAll(int flag);

    ApiResponse<MasQuestionOptionValueResponse> getById(Long id);

    ApiResponse<MasQuestionOptionValueResponse> create(MasQuestionOptionValueRequest request);

    ApiResponse<MasQuestionOptionValueResponse> update(Long id, MasQuestionOptionValueRequest request);

    ApiResponse<MasQuestionOptionValueResponse> changeStatus(Long id, String status);
}