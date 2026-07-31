package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasIpdBillingTypeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface MasIpdBillingTypeService {
    ApiResponse<List<MasIpdBillingTypeResponse>> getAllMasIpdBillingType(int flag);
}
