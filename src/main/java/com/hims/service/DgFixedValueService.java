package com.hims.service;

import com.hims.entity.DgFixedValue;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasCollectionResponse;

import java.util.List;

public interface DgFixedValueService {

    ApiResponse<List<DgFixedValue>> getDgFixedValue();


}
