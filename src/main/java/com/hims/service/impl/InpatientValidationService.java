package com.hims.service.impl;

import com.hims.entity.Inpatient;
import com.hims.entity.MasAdmissionStatus;
import com.hims.entity.repository.InpatientRepository;
import com.hims.exception.SDDException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InpatientValidationService {

    private final InpatientRepository inpatientRepository;

    public boolean isPatientCurrentlyAdmitted(Long patientId) {
        return inpatientRepository.findTopByPatient_IdOrderByInpatientIdDesc(patientId)
                .map(Inpatient::getAdmissionStatus)
                .map(MasAdmissionStatus::getStatusCode)
                .map(status -> "ADMITTED".equalsIgnoreCase(status))
                .orElse(false);
    }
}
