package com.hims.request;

import com.hims.entity.DentalProcedureTooth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureItemRequest {
    private Long procedureId;
    private Integer plannedSessionCount;
    private String remarks;
    private List<ProcedureSessionRequest> sessions;
    private List<DentalToothRequest> dentalProcedureTeeth;
}