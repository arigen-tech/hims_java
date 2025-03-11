package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasIdentificationTypeRequest {
    private String identificationCode;
    private String identificationName;
    private String status;
    private Long lastChangedBy;
    private Long mapId;
}