package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MasDistrictResponse {
    private Long id;
    private String districtName;
    private String status;
    private String lasChBy;
    private LocalDate lastChgDate;
    private Long stateId;
    private String stateName;
}
