package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasBloodCollectionTypeResponse {

    private Long collectionTypeId;
    private String collectionTypeCode;
    private String collectionTypeName;
    private String description;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}
