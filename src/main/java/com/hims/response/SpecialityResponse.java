package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SpecialityResponse {
    private Long specialityId;
    private String  specialityName;
    List<DoctorResponseList> doctorResponseListList;

    public SpecialityResponse() {

    }
}
