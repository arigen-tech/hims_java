package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RadiologyBillingResponse {
    private String registrationNo;
    private String mobileNo;
    private String appointmentDate;
    private String patientName;
    private String Age;
    private String Gender;
    private String BillingType;
    private Double billAmount;
    private Long billingHeaderId;
    private Long patientId;
    private String orderDate;
}
