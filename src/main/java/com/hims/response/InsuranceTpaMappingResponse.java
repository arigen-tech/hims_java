package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InsuranceTpaMappingResponse {
    private Long mappingId;
    private Long insuranceId;
    private String insuranceName;
    private Long tpaId;
    private String tpaName;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private String mode;

}