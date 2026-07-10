package com.hims.m1.abdm_response.createAbha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreateAbhaProfileResponse {


    private String firstName;
    private String middleName;
    private String lastName;
    private String dob;
    private String gender;
    private String photo;
    private String mobile;

    private List<String> phrAddress;
    private String address;

    private String districtCode;
    private String stateCode;
    private String pinCode;

    private String abhaType;
    private String stateName;
    private String districtName;

    @JsonProperty("ABHANumber")
    private String abhaNumber;

    private String abhaStatus;

}
