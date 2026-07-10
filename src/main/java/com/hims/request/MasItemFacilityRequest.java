package com.hims.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MasItemFacilityRequest {

    @NotBlank(message = "Facility code is required")
    private String facilityCode;

    @NotBlank(message = "Facility name is required")
    private String facilityName;

    private Long departmentId;
}