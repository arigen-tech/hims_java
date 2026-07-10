package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MasCrossMatchTypeResponse {
    private Long id;
    private String crossMatchCode;
    private String crossMatchName;
    private String description;
    private Integer turnaroundTimeMin;
    private BigDecimal chargeAmount;
    private String isEmergencyAllowed;
    private String status;
    private LocalDateTime createdDate;
    private String createdBy;
}
