package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MasHospitalResponseDto {
    private Long id;
    private String hospitalName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String executive1Contact;
    private String executive2Contact;
}
