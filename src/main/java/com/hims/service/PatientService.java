package com.hims.service;

import com.hims.entity.Patient;
import com.hims.request.OpdPatientDetailRequest;
import com.hims.request.PatientRequest;
import com.hims.request.VisitRequest;
import com.hims.response.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PatientService {
    ApiResponse<Patient> registerPatientWithOpd(PatientRequest patient, OpdPatientDetailRequest opdPatientDetail, VisitRequest visit);

    ApiResponse<Patient> updatePatient(PatientRequest request);
    ApiResponse<String> uploadImage(MultipartFile file);
}
