package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class DoctorDetailResponse {
    private Long doctorId;
    private String doctorName;
    private String gender;
    private String phoneNo;
    private String age;
    private Integer  yearsOfExperience;
    List<SpecialitiesResponse> specialitiesResponseList;
}
