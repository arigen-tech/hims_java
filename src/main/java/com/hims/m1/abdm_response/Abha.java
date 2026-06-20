package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Abha {
    private int index;

    @JsonProperty("ABHANumber")
    private String ABHANumber;
    private String name;
    private String gender;
    private String kycVerified;
}
