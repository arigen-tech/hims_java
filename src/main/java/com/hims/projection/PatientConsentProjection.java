package com.hims.projection;

import java.time.LocalDateTime;

public interface PatientConsentProjection {
    Long getPatientConsentId();
    Long getAdmissionId();
    Long getOtBookingId();
    Long getConsentTypeId();
    String getConsentTypeCode();
    String getConsentTypeName();
    Long getSurgeryId();
    String getSurgeryName();
    String getRemarks();
    String getDocumentName();
    String getUploadedBy();
    LocalDateTime getUploadedDatetime();
}
