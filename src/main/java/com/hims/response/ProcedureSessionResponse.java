package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureSessionResponse {

    private Long procedureSessionId;

    private Integer sessionNo;

    private LocalDateTime scheduledDateTime;

    private String remarks;

    private String sessionStatus;

    private String isFinalSession;

    private String billingStatus;
}