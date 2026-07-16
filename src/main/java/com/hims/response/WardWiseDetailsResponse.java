package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class WardWiseDetailsResponse {
    private Long patientId;
    private Long IpdPatientId;
    private String patientName;
    private Long roomId;
    private String roomName;
    private Long bedId;
    private String bedNumber;
    private LocalDate admitDate;
    private Long days;
    private String admissionNo;
    private String admissionStatus;
    private String ipdInternalStatus;
    private String age;
    private String gender;
    private String doctorName;




}
