package com.hims.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PendingToTransferResponse {
    private Long inpatientId;
    private Long patientId;
    private String transferNo;
    private LocalDateTime transferDateTime;
    private String patientName;
    private String gender;
    private String age;
    private String admissionNo;
    private String admissionDate;
    private Long fromWardId;
    private String fromWardName;
    private Long fromBedId;
    private String fromBedName;
    private Long toWardId;
    private String toWardName;
    private Long toBedId;
    private String toBedName;
    private Long transferReasonId;
    private String transferReason;
    private String transferStatus;
    private String clinicalNotes;
    private Long doctorId;
    private String doctorName;
    private String uhidNO;




}
