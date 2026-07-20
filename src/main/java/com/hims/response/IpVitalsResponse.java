package com.hims.response;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class IpVitalsResponse {
    private Long vitalId;
    private Long inpatientId;
    private LocalDateTime observationDatetime;
    private BigDecimal temperature;
    private Integer pulse;
    private Integer bpSystolic;
    private Integer bpDiastolic;
    private Integer respiration;
    private BigDecimal spo2;
    private Integer painScore;




}
