package com.hims.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MasConsentTypeResponse {
    private Long consentTypeId;
    private String consentTypeCode;
    private String consentTypeName;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
