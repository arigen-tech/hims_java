package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasBloodComponentResponse {
    private Long componentId;
    private String componentCode;
    private String componentName;
    private String description;
    private String storageTemp;
    private Integer shelfLifeDays;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}
