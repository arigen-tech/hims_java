package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasIpNursingAssessmentValueResponse;

import java.util.List;

public interface MasIpNursingAssessmentValueService {

    ApiResponse<List<MasIpNursingAssessmentValueResponse>> getAll(int flag);
}
