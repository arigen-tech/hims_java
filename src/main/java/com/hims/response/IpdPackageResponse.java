package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IpdPackageResponse {
    Long packageId;
    String packageName;
    String type;
    String departmentName;
    Integer stay;
    String inclusions;
    String exclusions;
    LocalDateTime lastUpdate;
    String status;
}
