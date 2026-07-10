package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasBloodBagTypeResponse {

    private Long bagTypeId;
    private String bagTypeCode;
    private String bagTypeName;
    private String description;
    private Integer maxComponents;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}
