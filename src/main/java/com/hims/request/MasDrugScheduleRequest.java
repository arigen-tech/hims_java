package com.hims.request;
// ========================= REQUEST DTO =========================

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MasDrugScheduleRequest {

    @NotBlank(message = "Schedule code is required")
    private String scheduleCode;

    @NotBlank(message = "Schedule name is required")
    private String scheduleName;

    private String legalDescription;


}