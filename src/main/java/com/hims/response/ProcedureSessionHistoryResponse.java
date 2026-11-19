package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcedureSessionHistoryResponse {

    private Long sessionId;
    private Integer sessionNumber;
    private LocalDateTime sessionDateTime;
    private String sessionStatus;
    private String isFinalSession;
    private String performedBy;
    private LocalDateTime performedAt;
    private String procedureFindings;
    private String procedureNotes;
    private String complicationFlag;
    private String complicationDetails;
    private String postProcedureAdvise;
}
