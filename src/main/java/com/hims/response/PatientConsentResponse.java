package com.hims.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PatientConsentResponse {
    private Long patientConsentId;
    private Long admissionId;
    private Long otBookingId;
    private Long consentTypeId;
    private String consentTypeCode;
    private String consentTypeName;
    private Long surgeryId;
    private String surgeryName;
    private String remarks;
    private String documentName;
    private String documentUrl;
    private String uploadedBy;
    private LocalDateTime uploadedDatetime;
}
