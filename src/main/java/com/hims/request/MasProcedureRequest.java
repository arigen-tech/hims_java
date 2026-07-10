package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasProcedureRequest {
    private String procedureCode;
    private String procedureName;
    private String opdAllowed;
    private String ipdAllowed;
    private String isNursing ;
    private String procedureLevel;
    private Long departmentId;
}
