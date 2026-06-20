package com.hims.m1.abdm_response.GetAbhaDetails;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class LocalizedLabels {
    private String name;
    private String abhaNumber;
    private String abhaAddress;
    private String gender;
    private String dob;
    private String mobile;
}
