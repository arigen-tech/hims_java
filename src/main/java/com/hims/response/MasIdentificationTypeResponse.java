package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasIdentificationTypeResponse {
    private Long identificationTypeId;
    private String identificationCode;
    private String identificationName;
    private String status;
    private Long lastChangedBy;
    private Instant lastChangedDate;
    private Long mapId;
}