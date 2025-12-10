package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class PendingBillingResponse {
    private Long visitId;
    private Long billinghdid;
    private Long patientid;
    private String patientName;
    private String mobileNo;
    private String age;
    private String sex;
    private String relation;
    private String billingType;
    private String consultedDoctor;
    private String department;
    private BigDecimal amount;
    private String billingStatus;
    private String address;
    private Integer  orderhdid;
    private String orderhdPaymentStatus;
    private String flag;
    private String source;
    private String patientUhid;

    private String visitType;
    private Long tokenNo;
    private Instant visitDate;
    private String sessionName;
    private BigDecimal registrationCost;


    private List<Long> billingHeaderIds;
    private List<AppointmentBlock> appointments;
    private List<BillingDetailResponse> details;



}
