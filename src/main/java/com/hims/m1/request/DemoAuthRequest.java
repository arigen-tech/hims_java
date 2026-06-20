package com.hims.m1.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoAuthRequest {

    @NotBlank(message = "AadhaarNumber cannot be null or empty.")
    private String aadhaarNumber;

    @NotBlank(message = "DistrictCode cannot be null or empty.")
    private String districtCode;

    @NotBlank(message = "StateCode cannot be null or empty.")
    private String stateCode;

    @NotBlank(message = "Year Of Birth cannot be null or empty.")
    private String yearOfBirth;

    @NotBlank(message = "Gender cannot be null or empty.")
    private String gender;

    @NotBlank(message = "Name cannot be null or empty.")
    private String name;

    @NotBlank(message = "Mobile cannot be null or empty.")
    private String mobile;

    @NotBlank(message = "Key cannot be null or empty.")
    private String key;

//    @NotBlank(message = "profilePhoto cannot be null or empty.")
//    private String profilePhoto;

//    @NotBlank(message = "Address cannot be null or empty.")
//    private String address;

//    @NotBlank(message = "PinCode cannot be null or empty.")
//    private String pincode;


//    @NotBlank(message = "benefitName cannot be null or empty.")
//    private String benefitName;


}
