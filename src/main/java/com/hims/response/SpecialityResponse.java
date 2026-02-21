package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SpecialityResponse {
    private Long specialityId;
    private String  specialityName;
    private Long hospitalId;
    private String hospitalName;
    List<DoctorResponseList> doctorResponseListList;

    public SpecialityResponse() {

    }
}
