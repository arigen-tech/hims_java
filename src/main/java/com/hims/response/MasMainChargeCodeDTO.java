package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class MasMainChargeCodeDTO {
    private Long chargecodeId;
    private String chargecodeCode;
    private String chargecodeName;
    private String status;
    private String lastChgBy;
    private LocalDate lastChgDate;
    private String lastChgTime;
}
