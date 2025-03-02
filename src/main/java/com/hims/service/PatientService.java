package com.hims.service;

import com.hims.dto.OpdPatientDetailDto;
import com.hims.dto.PatientDto;
import com.hims.response.ApiResponse;

public interface PatientService {
    ApiResponse<PatientDto> registerPatientWithOpd(PatientDto patientDto, OpdPatientDetailDto opdPatientDetailDto);
}
