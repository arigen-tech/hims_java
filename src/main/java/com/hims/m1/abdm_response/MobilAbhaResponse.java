package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MobilAbhaResponse {

    private String txnId;

    @JsonProperty("ABHA")
    private List<Abha> ABHA;


}
