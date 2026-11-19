package com.hims.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PatientConsentRequest {
    private Long admissionId;
    private Long otBookingId;
    @NotNull(message = "consentTypeId is required")
    private Long consentTypeId;
    private Long surgeryId;
    @Size(max = 1000)
    private String remarks;
    private MultipartFile document;
}
