package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data

public class SpecialitiesAndDoctorResponse {
    private List<SpecialitiesResponse> specialitiesResponseList;
    private List<DoctorResponse> doctorResponseList;


}
