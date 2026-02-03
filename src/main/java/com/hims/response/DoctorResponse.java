package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data

public class DoctorResponse {
    private Long doctorId;
    private String doctorName;
    private BigDecimal consultancyFee;



}
