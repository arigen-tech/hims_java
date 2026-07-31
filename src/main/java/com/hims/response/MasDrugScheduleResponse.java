package com.hims.response;
// ========================= RESPONSE DTO ========================

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasDrugScheduleResponse {

    private String scheduleCode;

    private String scheduleName;

    private String legalDescription;

    private String status;

    private LocalDateTime lastUpdateDate;


}