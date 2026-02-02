package com.hims.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BasicInfo {
    private String doctorName;
    private String age;
    private String gender;
    private Integer yearsOfExperience;
    private String profileDescription;
    private String phoneNo;
    private BigDecimal consultancyFee;
}
