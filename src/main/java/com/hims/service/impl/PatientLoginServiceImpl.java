package com.hims.service.impl;

import com.hims.entity.Patient;
import com.hims.entity.PatientLogin;
import com.hims.entity.repository.PatientLoginRepository;
import com.hims.service.PatientLoginService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PatientLoginServiceImpl implements PatientLoginService {

    @Autowired
    private PatientLoginRepository patientLoginRepository;

    @Override
    public PatientLogin savePatientLogin(Patient patient) {

        return patientLoginRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    PatientLogin login = new PatientLogin();
                    login.setPatientId(patient.getId());
                    login.setMobileNo(patient.getPatientMobileNumber());
                    login.setEmail(patient.getPatientEmailId());
                    login.setStatus("A");
                    return patientLoginRepository.save(login);
                });
    }
}
