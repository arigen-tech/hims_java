package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class WardWiseDetailsResponse {
    private Long patientId;
    private Long IpdPatientId;
    private String patientName;
    private String wardName;
    private String roomName;
    private String bedNumber;
    private LocalDate admitDate;
    private Long days;
    private Long bedCount;
    private String admissionNo;
    private String admissionStatus;
    private String ipdInternalStatus;


}
