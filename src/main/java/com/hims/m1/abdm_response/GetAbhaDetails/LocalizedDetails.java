package com.hims.m1.abdm_response.GetAbhaDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class LocalizedDetails {
    private String name;
    private String stateName;
    private String districtName;
    private String villageName;
    private String townName;
    private String gender;

    private LocalizedLabels localizedLabels;
}
