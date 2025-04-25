package com.hims.response;

import lombok.Data;

import java.time.Instant;

@Data
public class MasMainChargeCodeDTO {
    private Long chargecodeId;
    private String chargecodeCode;
    private String chargecodeName;
    private String status;
    private String lastChgBy;
    private Instant lastChgDate;
    private String lastChgTime;
}
