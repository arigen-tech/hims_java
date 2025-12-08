package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpdPatientVitalResponce {

    private Long opdPatientDetailsId;
    private String height;
    private String weight;
    private String pulse;
    private String temperature;
    private String rr;
    private String bmi;
    private String spo2;
    private String bpSystolic;
    private String bpDiastolic;
    private String mlcFlag;

}
