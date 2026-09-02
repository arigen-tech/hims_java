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
public class DentalProcedureResponse {

    private Long procedureHdId;
    private String procedureNo;

    private Long patientId;
    private Long visitId;
    private Long departmentId;

    private String diagnosis;
    private String procedureTypeCode;

    private List<ProcedureItemResponse> procedureDetails;
}