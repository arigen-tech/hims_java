package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class PackageInvestigationMappingRequest {
    private Long packageId;
    private List<Long> investigationIds;
}
