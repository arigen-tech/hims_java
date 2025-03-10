package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasStateRequest {
    private String stateCode;
    private String stateName;
    private String status;
    private String lastChgBy;
    private Long countryId;
}