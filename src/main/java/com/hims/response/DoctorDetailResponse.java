package com.hims.response;

import com.hims.entity.BasicInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DoctorDetailResponse {
    private Long doctorId;
    private String hospitalName;
    private List<AppSetResponse> appSetResponseList;

    private BasicInfo basicInfo;
    private List<SpecialitiesResponse> specialitiesResponseList;
    private List<String> education;
    private List<String> workExperience;
    private List<String> memberships;
    private List<String> specialtyInterests;
    private List<String> awardsAndDistinctions;
    private List<String> languages;






}
