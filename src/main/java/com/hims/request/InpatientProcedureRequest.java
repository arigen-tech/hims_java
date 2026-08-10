package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InpatientProcedureRequest {

    private Long inpatientId;
    private Long procedureId;
    private LocalDateTime procedureDatetime;
    private String performedBy;
    private String remarks;


}