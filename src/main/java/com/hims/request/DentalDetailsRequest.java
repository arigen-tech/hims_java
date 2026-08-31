package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class DentalDetailsRequest {

    private Long patientId;
    private Long visitId;
    private DentalExaminationRequest dentalExamination;
    private ProcedureRequestHd proceduresDetails;
//    private List<DentalProcedureToothRequest> dentalProcedureTeeth;
}
