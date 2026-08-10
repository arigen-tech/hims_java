package com.hims.service;

import com.hims.request.MasWardRoomTariffRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasWardRoomTariffResponse;

import java.util.List;

public interface MasWardRoomTariffService {

    ApiResponse<MasWardRoomTariffResponse> createWardRoomTariff(MasWardRoomTariffRequest request);

    ApiResponse<MasWardRoomTariffResponse> updateWardRoomTariff(Long tariffId, MasWardRoomTariffRequest request);

    ApiResponse<MasWardRoomTariffResponse> changeActiveStatus(Long tariffId, String status);

    ApiResponse<MasWardRoomTariffResponse> getWardRoomTariffById(Long tariffId);

    ApiResponse<List<MasWardRoomTariffResponse>> getAllWardRoomTariffs(int flag);

    ApiResponse<List<MasWardRoomTariffResponse>> getTariffsByWardAndRoom(Long wardId, Long roomId);
}