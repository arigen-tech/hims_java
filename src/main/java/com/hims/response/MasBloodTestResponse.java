package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasBloodTestResponse {
    private Long bloodTestId;
    private String testCode;
    private String testName;
    private String isMandatory;
    private Long applicableCollectionTypeId;
    private String status;
    private LocalDateTime createdDate;
    private String createdBy;
}
