package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MasDesignationResponse {
    private Long designationId;
    private String designationName;

    private Long userTypeId;
    private String userTypeName;

    private String status;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdateDate;
}
