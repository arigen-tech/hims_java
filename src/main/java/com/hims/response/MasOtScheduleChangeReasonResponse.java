package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasOtScheduleChangeReasonResponse {

    private Long reasonId;
    private String reason;
    private String applicableFor;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
