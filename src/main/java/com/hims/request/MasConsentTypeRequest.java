package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MasConsentTypeRequest {
    @NotBlank(message = "Consent type code is required")
    @Size(max = 50)
    private String consentTypeCode;
    @NotBlank(message = "Consent type name is required")
    @Size(max = 200)
    private String consentTypeName;
}
