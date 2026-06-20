package com.hims.m1.service;


import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.response.MasterResponse;

import java.util.List;

public interface MasterService {

    ApiResponse<List<MasterResponse>> getVerificationType();

}
