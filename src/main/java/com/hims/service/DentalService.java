package com.hims.service;

import com.hims.entity.Patient;
import com.hims.entity.User;
import com.hims.entity.Visit;
import com.hims.request.DentalDetailsRequest;
import com.hims.response.ApiResponse;

public interface DentalService {

    ApiResponse<String> createOrUpdateDentalDetails(
            DentalDetailsRequest request,
            Patient patient,
            Visit visit,
            User user,
            Long departmentId
    );
}