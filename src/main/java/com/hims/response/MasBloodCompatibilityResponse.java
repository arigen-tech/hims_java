package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasBloodCompatibilityResponse {

    private Long compatibilityId;
    private Long componentId;
    private String componentName;
    private Long patientBloodGroupId;
    private String patientBloodGroup;
    private Long donorBloodGroupId;
    private String donorBloodGroup;
    private String isPreferred;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}
