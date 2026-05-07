package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PriviousVitalsDetailsByPatientResponse {
    private LocalDate visitDate;
    private String pulse;
    private String height;
private  String weight;
private String temperature;
private String rr;
private String bmi;
private String spo2;
private String bpSystolic;
    private String bpDiastolic;


}
