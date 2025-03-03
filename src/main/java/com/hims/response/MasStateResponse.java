package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasStateResponse {
    private Long id;
    private String stateCode;
    private String stateName;
    private String status;
    private String lastChgBy;
    private Instant lastChgDate;
    private String lastChgTime;
    private Long countryId;
    private String countryName;
}
