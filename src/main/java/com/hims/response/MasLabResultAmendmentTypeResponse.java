package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasLabResultAmendmentTypeResponse {

    private Long amendmentTypeId;
    private String amendmentTypeCode;
    private String amendmentTypeName;
    private String description;
    private String status;
    private LocalDateTime lastUpdateDate;
}
