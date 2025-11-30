package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.Visit;
import com.hims.entity.repository.PatientRepository;
import com.hims.entity.repository.VisitRepository;
import com.hims.response.ApiResponse;
import com.hims.response.VisitResponse;
import com.hims.service.ClinicalHistoryService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class clinicalHistoryServiceImpl implements ClinicalHistoryService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private VisitRepository visitRepo;

    @Override
    public ApiResponse<List<VisitResponse>> getPreviousVisits(Integer patient) {
        List<Visit> visits= visitRepo.findByPatientId(patient);
        List<VisitResponse> responses = visits.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });
    }

    private VisitResponse mapToResponse(Visit entity) {
        VisitResponse dto = new VisitResponse();
        dto.setId(entity.getId());
        dto.setTokenNo(entity.getTokenNo());
        dto.setVisitDate(entity.getVisitDate());
        dto.setLastChgDate(entity.getLastChgDate());
        dto.setVisitStatus(entity.getVisitStatus());
        dto.setPriority(entity.getPriority());
        dto.setDepartmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null);
        dto.setDepartmentName(entity.getDepartment() != null ? entity.getDepartment().getDepartmentName() : null);
//        dto.setDoctorId(entity.getDoctor().getid);
        dto.setDoctorName(entity.getDoctorName());
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        dto.setHospitalId(entity.getHospital() != null ? entity.getHospital().getId() : null);
        dto.setHospitalName(entity.getHospital() != null ? entity.getHospital().getHospitalName(): null);
//        dto.setIniDoctor(entity.getIniDoctor());
        dto.setSessionId(entity.getSession()!= null ? entity.getSession().getId() : null) ;
        dto.setBillingStatus(entity.getBillingStatus());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setPreConsultation(entity.getPreConsultation());
        dto.setBillingHdId(entity.getBillingHd()!= null ? entity.getBillingHd().getId() : null);
        return dto;
    }

}
