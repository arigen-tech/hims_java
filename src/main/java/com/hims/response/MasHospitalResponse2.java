package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MasHospitalResponse2 {
    private Long id;
    private String hospitalName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String executive1Contact;
    private String executive2Contact;
}
