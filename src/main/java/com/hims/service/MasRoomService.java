package com.hims.service;

import com.hims.request.MasRoomRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRoomResponse;

import java.util.List;

public interface MasRoomService {

    ApiResponse<MasRoomResponse> createRoom(MasRoomRequest request);

    ApiResponse<MasRoomResponse> updateRoom(Long roomId, MasRoomRequest request);

    ApiResponse<MasRoomResponse> changeActiveStatus(Long roomId, String status);

    ApiResponse<MasRoomResponse> getById(Long roomId);

    ApiResponse<List<MasRoomResponse>> getAll(int flag);
}
