package com.hims.m1.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(
        value = {"hibernateLazyInitializer", "handler"},
        ignoreUnknown = true
)
@Data
public class GetAbhaProfileResponse {


    @JsonProperty("ABHANumber")
    private String abhaNumber;
    private String preferredAbhaAddress;
    private String mobile;
    private String addharNo;
    private String name;
    private String hindiName;
    private String hindiGender;
    private String dayOfBirth;
    private String gender;
    private String profilePhoto;
    private String stateName;
    private String districtName;
    private byte[]  qrPath;

    private String address;
    private String subdistrictName;

    private String pincode;
    private String districtCode;
    private String stateCode;
}
