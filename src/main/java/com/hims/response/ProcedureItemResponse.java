package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureItemResponse {

    private Long procedureDtId;

    private Long procedureId;
    private String procedureName;

    private Integer plannedSessionCount;
    private Integer completedSessionCount;

    private String remarks;

    private List<ProcedureSessionResponse> sessions;

    private List<DentalToothResponse> dentalProcedureTeeth;
}