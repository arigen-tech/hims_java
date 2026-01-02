package com.hims.service;

import com.hims.request.MasQuestionHeadingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasQuestionHeadingResponse;

import java.util.List;

public interface MasQuestionHeadingService {

    ApiResponse<List<MasQuestionHeadingResponse>> getAll(int flag);

    ApiResponse<MasQuestionHeadingResponse> getById(Long id);

    ApiResponse<MasQuestionHeadingResponse> create(
            MasQuestionHeadingRequest request);

    ApiResponse<MasQuestionHeadingResponse> update(
            Long id, MasQuestionHeadingRequest request);

    ApiResponse<MasQuestionHeadingResponse> changeStatus(
            Long id, String status);
}
