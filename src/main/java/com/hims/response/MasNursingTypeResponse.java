package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasNursingTypeResponse {
    private Long nursingTypeId;
    private String nursingTypeName;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
    private String description;
}
