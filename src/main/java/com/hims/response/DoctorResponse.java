package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data

public class DoctorResponse {
    private Long doctorId;
    private String doctorName;
    private BigDecimal consultancyFee;
    private Integer yearOfExperience;
    private List<SessionResponseList> sessionResponseLists;



}
