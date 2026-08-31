package com.hims.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProcedureRequestHd {

    private Long patientId;
    private Long visitId;
    private Long departmentId;
    private Long hospitalId;
    private Long doctorId;
    private String diagnosis;
    private String procedureTypeCode;

    private List<ProcedureItemRequest> procedureDetails;
}
