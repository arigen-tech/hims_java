package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasAdmissionTypeResponse {
    private Long admissionTypeId;
    private String admissionTypeName;
    private String description;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}
