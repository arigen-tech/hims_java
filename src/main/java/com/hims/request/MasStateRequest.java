package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasStateRequest {
    private String stateCode;
    private String stateName;
    private Long countryId;
}