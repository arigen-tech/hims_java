package com.hims.service;

import com.hims.request.OpdObgDetailsRequest;
import com.hims.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


public interface OpdObgDetailsService {
    ApiResponse<String> saveObgDetails(@Valid OpdObgDetailsRequest request);
}
