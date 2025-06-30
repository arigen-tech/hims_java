package com.hims.service;

import com.hims.request.MasOpdSessionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOpdSessionResponse;

import java.util.List;

public interface MasOpdSessionService {
    ApiResponse<List<MasOpdSessionResponse>> getAllOpdSessions(int flag);
    ApiResponse<MasOpdSessionResponse> findById(Long id);
    public ApiResponse<MasOpdSessionResponse> addSession(MasOpdSessionRequest request);
    public ApiResponse<MasOpdSessionResponse> updateSession(Long id, MasOpdSessionRequest request);
    ApiResponse<MasOpdSessionResponse> changeOpdSessionStatus(Long id, String status);
}
