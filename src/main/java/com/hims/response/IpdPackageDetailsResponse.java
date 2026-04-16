package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IpdPackageDetailsResponse {
    String packageName;
    String type;
    String departmentName;
    Integer stay;
    String inclusions;
    String exclusions;
    LocalDateTime lastUpdate;
    String status;
    List<MasIpdPackageInclusionResponse> masIpdPackageInclusionResponses;

}
