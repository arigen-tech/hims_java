package com.hims.service;


import com.hims.request.LoginRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MobileLoginResponce;


public interface MobileLoginService {
    ApiResponse<MobileLoginResponce> loginRequest(LoginRequest request);


}
