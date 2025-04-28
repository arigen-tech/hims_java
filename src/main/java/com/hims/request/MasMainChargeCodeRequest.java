package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasMainChargeCodeRequest {
    private String chargecode_code;
    private String chargecode_name;
    private String status;
//    private String lastChgBy;
//    private Instant lastChgDate;
//    private String lastChgTime;
}