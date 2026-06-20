package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbhaAddressSuggestionRequest {


    private String txnId;
    private List<String> abhaAddressList;
}
