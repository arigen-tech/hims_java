package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillingHeaderResponse {
    private Long headerId;
    private Long visitId;
    private String billNo;
    private String patientName;
    private String phoneNo;
    private String age;
    private String relation;
    private String sex;
    private String department;
    private String billDate;
    private BigDecimal netAmount;
    private Long serviceCategoryId;
    private String serviceCategoryName;
    private String paymentStatus;




}
