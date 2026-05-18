package com.hims.request;

import lombok.Data;

import java.time.LocalDate;


    @Data
    public class InsuranceTpaMappingRequest {
        private Long insuranceId;
        private Long tpaId;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private  String mode;
    }

