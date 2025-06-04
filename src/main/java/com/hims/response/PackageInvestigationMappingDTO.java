package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class PackageInvestigationMappingDTO {
    private Long pimId;
    private Long packageId;
//    private Long investId;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
}
