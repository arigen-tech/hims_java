package com.hims.m1.service;


import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.request.DemoAuthRequest;
import com.hims.m1.response.AUthDeamoResponse;
import reactor.core.publisher.Mono;

public interface AuthDemoAbhaService {

    Mono<ApiResponse<AUthDeamoResponse>> authDemo(DemoAuthRequest demoAuthRequest) throws Exception;

}
