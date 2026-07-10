package com.hims.service;

import com.hims.request.TransferReasonRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTransferReasonResponse;

import java.util.List;

public interface TransferReasonService {

    ApiResponse<MasTransferReasonResponse> createTransferReason(TransferReasonRequest request);

    ApiResponse<MasTransferReasonResponse> updateTransferReason(Long transferReasonId, TransferReasonRequest request);

    ApiResponse<MasTransferReasonResponse> changeActiveStatus(Long transferReasonId, String status);

    ApiResponse<MasTransferReasonResponse> getById(Long transferReasonId);

    ApiResponse<List<MasTransferReasonResponse>> getAll(int flag);
}