package com.hims.service;

import com.hims.entity.Patient;
import com.hims.entity.PatientLogin;

public interface PatientLoginService {
    PatientLogin savePatientLogin(Patient patient);
}
