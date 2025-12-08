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
    private String defaultStatus;
    private String status;
    private String procedureGroup;
    private String departmentName;
    private String procedureTypeName;
    private Long procedureTypeId;
    private String lastChangedBy;
    private LocalDateTime lastChangedDate;

}
