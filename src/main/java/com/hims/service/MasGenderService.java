package com.hims.service;

import com.hims.entity.MasGender;
import com.hims.response.ApiResponse;
import com.hims.response.MasGenderResponse;

import java.util.List;

public interface MasGenderService {
    ApiResponse<List<MasGenderResponse>> getAllGenders();
}
