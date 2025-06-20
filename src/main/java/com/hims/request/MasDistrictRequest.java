package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasDistrictRequest {
    private String districtName;
    private Long stateId;
}