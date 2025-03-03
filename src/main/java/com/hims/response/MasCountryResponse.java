package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasCountryResponse {
    private Long id;
    private String countryCode;
    private String countryName;
    private String status;
    private String lastChgBy;
    private Instant lastChgDate;
    private String lastChgTime;
}
