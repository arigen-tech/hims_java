package com.hims.service;

import com.hims.request.MasQuestionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasQuestionResponse;

import java.util.List;

public interface MasQuestionService {

    ApiResponse<List<MasQuestionResponse>> getAll(int flag);

    ApiResponse<MasQuestionResponse> getById(Long id);

    ApiResponse<MasQuestionResponse> create(MasQuestionRequest request);

    ApiResponse<MasQuestionResponse> update(Long id, MasQuestionRequest request);

    ApiResponse<MasQuestionResponse> changeStatus(Long id, String status);
}
