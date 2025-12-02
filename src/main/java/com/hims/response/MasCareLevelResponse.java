package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MasCareLevelResponse {

    private Long careId;
    private String careLevelName;
    private String description;
    private String status;
    private LocalDate lastUpdateDate;
}
