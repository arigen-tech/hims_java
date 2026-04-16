package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BloodDonorPriviousScreening {
    private Long screeningId;
    private LocalDate screeningDate;
    private BigDecimal hemoglobin;
    private BigDecimal weight;
    private BigDecimal height;
    private String bp;
    private Integer pulse;
    private BigDecimal temperature;
    private String screeningResult;
    private String deferralType;
    private  String deferralReason;
   private String conductedBy;

}
