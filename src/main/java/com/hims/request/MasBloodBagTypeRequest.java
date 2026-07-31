package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodBagTypeRequest {

    private String bagTypeCode;
    private String bagTypeName;
    private String description;
    private Integer maxComponents;
}
