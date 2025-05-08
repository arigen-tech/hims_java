package com.hims.service;

import com.hims.entity.Patient;
import com.hims.entity.Visit;
import com.hims.request.*;
import com.hims.response.ApiResponse;
import com.hims.response.PatientRegFollowUpResp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PatientService {
    ApiResponse<PatientRegFollowUpResp> registerPatientWithOpd(PatientRequest patient, OpdPatientDetailRequest opdPatientDetail, VisitRequest visit);

    ApiResponse<PatientRegFollowUpResp> updatePatient(PatientFollowUpReq request);
    ApiResponse<String> uploadImage(MultipartFile file);

    ApiResponse<List<Patient>> searchPatient(PatientSearchReq substring);

    ApiResponse<List<Visit>> getPendingPreConsultations();
}
