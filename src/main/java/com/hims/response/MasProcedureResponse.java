package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasProcedureResponse {
    private Long procedureId;
    private String procedureCode;
    private String procedureName;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
    private Long departmentId;
    private String departmentName;
    private String opdAllowed;
    private String ipdAllowed;
    private String isNursing;
    private String procedureLevel;

}
