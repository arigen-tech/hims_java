package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class MasServiceOpdRequest {
    private BigDecimal baseTariff;
    private Long serviceCategory;
    private Long hospitalId;
    private Long departmentId;
    private Long doctorId;
    private Instant fromDate;
    private Instant toDate;
}
