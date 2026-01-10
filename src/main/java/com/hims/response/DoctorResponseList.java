package com.hims.response;

import lombok.Data;

@Data
public class DoctorResponseList {
    private Long doctorId;
    private String doctorName;
    private String specialityName;
    private String gender;
    private String phoneNo;
    private String age;
    private Integer  yearsOfExperience;
}
