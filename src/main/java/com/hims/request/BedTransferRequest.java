package com.hims.request;

import lombok.Data;

@Data
public class BedTransferRequest {
    private Long inpatientId;
    private Long patientId;
    private Long fromWard;
    private Long fromBed;
    private Long toWard;
    private Long toBed;
    private Long doctorId;
    private String priority;
    private Long transferReasonId;
    private String clinicalNotes;
}
