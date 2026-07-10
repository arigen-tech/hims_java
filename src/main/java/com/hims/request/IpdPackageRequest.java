package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class IpdPackageRequest {
     private String packageName;
     private   Long packageTypeId;
    private Long departmentId;
    private  Integer stayDays;
    private String generatedInclusions;
    private String generatedExclusions;

    List<MasIpdPackageInclusionRequest> masIpdPackageInclusionRequestList;
}
