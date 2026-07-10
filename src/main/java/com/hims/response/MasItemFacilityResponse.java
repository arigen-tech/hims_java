package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasItemFacilityResponse {

    private Long facilityId;

    private String facilityCode;

    private String facilityName;

    private Long departmentId;

    private String departmentName;

    private String status;


}