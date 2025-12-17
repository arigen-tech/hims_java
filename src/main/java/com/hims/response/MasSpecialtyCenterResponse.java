package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class MasSpecialtyCenterResponse {
    private Long centerId;
    private String centerName;
    private String description;
    private String status;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdateDate;
}
