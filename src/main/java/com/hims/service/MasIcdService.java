package com.hims.service;

import com.hims.entity.MasIcd;
import com.hims.response.ApiResponse;
import com.hims.response.MasIcdResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MasIcdService {

//    ApiResponse<List<MasIcdResponse>> getAllIcds(int flag);


    ApiResponse<Page<MasIcdResponse>> getAllIcd(int flag, int page, int size, String search);
}
