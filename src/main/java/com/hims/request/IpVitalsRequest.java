package com.hims.request;

import com.hims.entity.Inpatient;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IpVitalsRequest {
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
