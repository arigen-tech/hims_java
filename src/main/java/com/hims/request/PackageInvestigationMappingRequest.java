package com.hims.request;

import lombok.Data;

@Data
public class PackageInvestigationMappingRequest {
    private Long packageId;
//    private Long investId;
    private String status;
}
