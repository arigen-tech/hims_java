package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasCountryRequest {
    private String countryCode;
    private String countryName;
}