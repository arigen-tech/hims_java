package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class MasSubChargeCodeDTO {
    private Long subId;
    private String subCode;
    private String subName;
    private String status;
    private String lastChgBy;
    private LocalDate lastChgDate;
    private String lastChgTime;
    private Long mainChargeId;
}
