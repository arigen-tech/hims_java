package com.hims.m1.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AUthDeamoResponse {


    private String healthIdNumber;
    private String healthId;
    private String mobile;

    private String firstName;
    private String middleName;
    private String lastName;
    private String name;

    private String yearOfBirth;
    private String dayOfBirth;
    private String monthOfBirth;

    private String gender;

    private String stateCode;
    private String districtCode;
    private String stateName;
    private String districtName;

    private Boolean kycVerified;

    @JsonProperty("token")
    private String xToken;

    private String status;

    @JsonProperty("new")
    private Boolean isNew;   // maps "new"

    private String isType = "1";
}
