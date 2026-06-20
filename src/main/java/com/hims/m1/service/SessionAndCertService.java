package com.hims.m1.service;


import com.hims.m1.abdm_response.AbdmCertificateResponse;
import com.hims.m1.abdm_response.AbdmSessionApiResponse;
import com.hims.m1.apiResponse.ApiResponse;
import reactor.core.publisher.Mono;

public interface SessionAndCertService {

    Mono<ApiResponse<AbdmCertificateResponse>> getCertificateABDM();
    Mono<ApiResponse<AbdmSessionApiResponse>> fetchTokenFromApi();

}
