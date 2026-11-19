package com.hims.mapper;

import com.hims.projection.PatientConsentProjection;
import com.hims.response.PatientConsentResponse;
import org.springframework.stereotype.Component;

@Component
public class PatientConsentMapper {
    public PatientConsentResponse toResponse(PatientConsentProjection row) {
        PatientConsentResponse response = new PatientConsentResponse();
        response.setPatientConsentId(row.getPatientConsentId());
        response.setAdmissionId(row.getAdmissionId());
        response.setOtBookingId(row.getOtBookingId());
        response.setConsentTypeId(row.getConsentTypeId());
        response.setConsentTypeCode(row.getConsentTypeCode());
        response.setConsentTypeName(row.getConsentTypeName());
        response.setSurgeryId(row.getSurgeryId());
        response.setSurgeryName(row.getSurgeryName());
        response.setRemarks(row.getRemarks());
        response.setDocumentName(row.getDocumentName());
        response.setDocumentUrl("/otController/patientConsent/document/" + row.getPatientConsentId());
        response.setUploadedBy(row.getUploadedBy());
        response.setUploadedDatetime(row.getUploadedDatetime());
        return response;
    }
}
