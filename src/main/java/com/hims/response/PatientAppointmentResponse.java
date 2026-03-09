package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PatientAppointmentResponse {

    private Long patientid;
    private String patientName;
    private String mobileNo;
    private String age;
    private String gender;
    private String relation;
    private String address;
    private String patientUhid;

    private List<AppointmentBlock> appointments;

}
