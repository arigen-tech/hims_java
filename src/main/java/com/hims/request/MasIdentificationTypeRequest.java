package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasIdentificationTypeRequest {
    private String identificationCode;
    private String identificationName;
    private Long mapId;
}