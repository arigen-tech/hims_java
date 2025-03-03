package com.hims.service;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.response.ApiResponse;

public interface PatientService {
    ApiResponse<Patient> registerPatientWithOpd(Patient patient, OpdPatientDetail opdPatientDetail);
}
