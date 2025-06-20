package com.hims.request;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class MasHsnRequest {

    private String hsnCode;
    private BigDecimal gstRate;
    private Boolean isMedicine;
    private String hsnCategory;
    private String hsnSubcategory;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;



}
