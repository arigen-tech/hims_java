package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasPaymentGatewayResponse;

import java.util.List;

public interface MasPaymentGatewayService {
   ApiResponse<List<MasPaymentGatewayResponse>> getMasPaymentGateways(int flag);
}
